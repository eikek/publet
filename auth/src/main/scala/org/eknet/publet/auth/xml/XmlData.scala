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

import scala.xml.{Node, Elem}
import org.eknet.publet.auth.store.{ResourcePatternDef, UserProperty, User}
import org.eknet.publet.vfs.ContentResource
import org.eknet.publet.auth.ResourceAction
import org.eknet.publet.Glob
import collection.mutable
import org.apache.shiro.SecurityUtils

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 01.11.12 22:44
 */
class XmlData(source: ContentResource) extends XmlResource(source) {

  private var _data = new Data

  class Data {
    var users = Map[String, User]()
    var groups = Map[String, Set[String]]()
    var permissions = Map[String, Set[String]]()
    var anonPatterns = List[String]()
    var restricted = List[ResourcePatternDef]()
  }

  reload()

  def onLoad(rootElem: Elem) {
    withWriteLock {
      val data = new Data
      val umap = mutable.Map[String, User]()
      val gmap = mutable.Map[String, Set[String]]()
      (rootElem \ "users" \ "user").map(un => {
        val user = userFromXml(un)
        val groups = groupsFromXml(un)
        umap.put(user.login, user)
        gmap.put(user.login, groups)
      })
      data.users = umap.toMap
      data.groups = gmap.toMap

      val pmap = mutable.Map[String, Set[String]]()
      (rootElem \ "permissions" \ "grant")
        .map(pnm => permissionFromXml(pnm))
        .foreach(pm => {
          for (g <-pm.groups) {
            val set = data.permissions.get(g).getOrElse(Set[String]()) ++ pm.perms
            pmap.put(g, set)
          }
      })
      data.permissions = pmap.toMap

      data.anonPatterns ++= (rootElem \ "resources" \ "open").map(_.text).toList
      data.restricted ++= (rootElem \ "resources" \ "restricted").map(restrictedFromXml).toList

      this._data = data
    }
  }

  def toXml = <publetAuth>
    <users>
      { _data.users.values.map(u => userToXml(u, _data.groups.get(u.login).getOrElse(Set()))) }
    </users>
    <permissions>
      { for (g <- _data.permissions.keys) yield {
           <grant><to>{g}</to>{ _data.permissions.get(g).get.map(perm => <perm>{perm}</perm>)}</grant>
        }
      }
    </permissions>
    <resources>
      { _data.anonPatterns.map(rc => <open>{rc}</open> ) }
      { _data.restricted.map(r => restrictedToXml(r) ) }
    </resources>
  </publetAuth>

  private def userToXml(user: User, groups: Set[String]): Elem = {
    val props = for (uv <- UserProperty.values if (user.properties.contains(uv))) yield {
      <a>{user.properties.get(uv).get}</a>.copy(label = uv.toString)
    }
    <user login={ user.login }>
      { props ++ groupsToXml(groups) }
    </user>
  }

  private def userFromXml(userN: Node): User = {
    val login = (userN \ "@login").text.trim
    val attr = (for (c <- userN.child if (UserProperty.exists(c.label.trim))) yield (UserProperty.withName(c.label.trim) -> c.text)).toMap
    User(login, attr)
  }

  private def groupsFromXml(userN: Node): Set[String] = {
    (for (c <- userN \ "group") yield c.text.trim).toSet
  }

  private def groupsToXml(groups: Set[String]) = for (g <- groups) yield <group>{g}</group>


  private def permissionFromXml(node: Node): PermissionModel = {
    val groups = (node \ "to").map(_.text.trim).toList
    val perms = (node \ "perm").map(_.text.trim).toList
    PermissionModel(groups.toSet, perms.toSet)
  }

  private def restrictedFromXml(node: Node): ResourcePatternDef = {
    val on = (node \ "@on").text.trim
    val by = (node \ "@by").text.trim
    val actions = if (!on.isEmpty) {
      ResourceAction.forName(on)
    } else ResourceAction.all
    val pattern = Glob(node.text.trim)
    ResourcePatternDef(pattern, by, actions)
  }

  private def restrictedToXml(rdef: ResourcePatternDef) = {
    rdef match {
      case ResourcePatternDef(Glob(pattern), "", ResourceAction.all) => {
        <restricted>
          { pattern }
        </restricted>
      }
      case ResourcePatternDef(Glob(pattern), "", action) => {
        <restricted on={action.name}>
          { pattern }
        </restricted>
      }
      case ResourcePatternDef(Glob(pattern), by, on) => {
        <restricted on={on.name} by={by}>
          { pattern }
        </restricted>
      }
    }
  }

  def users = withReadLock( _data.users )
  def groups = withReadLock( _data.groups )
  def permissions = withReadLock( _data.permissions )
  def anonPatterns = withReadLock( _data.anonPatterns )
  def restrictedPatterns = withReadLock( _data.restricted )

  def modify[A](f: (Data) => A): A = {
    withWriteLock {
      val r = f(_data)
      val login = SecurityUtils.getSubject.getPrincipal.toString
      write(users.get(login), "Writing auth data.")
      r
    }
  }
}
