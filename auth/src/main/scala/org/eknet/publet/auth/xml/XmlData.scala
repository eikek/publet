/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.publet.auth.xml

import scala.xml.Elem
import org.eknet.publet.auth.user.User
import org.eknet.publet.vfs.ContentResource
import org.eknet.publet.auth.repository.RepositoryModel
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 01.11.12 22:44
 */
class XmlData(source: ContentResource) extends XmlResource(source) {

  private val lock = new ReentrantReadWriteLock()

  private var _users = Map[String, User]()
  private var _groups = Map[String, Set[String]]()
  private var _repositories = Map[String, RepositoryModel]()
  private var _permissions = Map[String, Set[String]]()
  private var _anonPatterns = List[String]()

  reload()

  def onLoad(rootElem: Elem) {
    lock.writeLock().lock()
    try {
      val umap = collection.mutable.Map[String, User]()
      val gmap = collection.mutable.Map[String, Set[String]]()
      (rootElem \ "users" \ "user").map(un => {
        val user = Serializer.userFromXml(un)
        val groups = Serializer.groupsFromXml(un)
        umap.put(user.login, user)
        gmap.put(user.login, groups)
      })
      this._users = umap.toMap
      this._groups = gmap.toMap

      this._repositories = (rootElem \ "repositories" \ "repository")
        .map(Serializer.repositoryFromXml(_))
        .map(rm => rm.name -> rm)
        .toMap

      val pmap = collection.mutable.Map[String, Set[String]]()
      (rootElem \ "permissions" \ "grant")
        .map(pnm => Serializer.permissionFromXml(pnm))
        .foreach(pm => {
          for (g <-pm.groups) {
            val set = pmap.get(g).getOrElse(Set[String]()) ++ pm.perms
            pmap.put(g, set)
          }
      })
      this._permissions = pmap.toMap

      this._anonPatterns = (rootElem \ "anonymous" \ "pattern")
        .map(_.text).toList

    } finally {
      lock.writeLock().unlock()
    }
  }


  override def write(currentUser: Option[User], message: String) {
    lock.writeLock().lock()
    try {
      super.write(currentUser, message)
    } finally {
      lock.writeLock().unlock()
    }
  }

  def toXml = <publetAuth>
    <users>
      { _users.values.map(u => Serializer.userToXml(u, _groups.get(u.login).getOrElse(Set()))) }
    </users>
    <repositories>
      { _repositories.values.map(rm => Serializer.repositoryToXml(rm)) }
    </repositories>
    <permissions>
      { for (g <- _permissions.keys) yield {
           <grant><to>{g}</to>{ _permissions.get(g).get.map(perm => <perm>{perm}</perm>)}</grant>
        }
      }
    </permissions>
    <anonymous>
      { _anonPatterns.map(rc => <pattern>{rc}</pattern> )}
    </anonymous>
  </publetAuth>


  def users = _users
  def groups = _groups
  def repositories = _repositories
  def permissions = _permissions
  def anonPatterns = _anonPatterns
}
