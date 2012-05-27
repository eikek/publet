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


import scala.xml.{PrettyPrinter, XML}
import org.eknet.publet.vfs.{Writeable, ContentResource}
import java.io.ByteArrayInputStream
import org.eknet.publet.auth._
import org.eknet.publet.engine.Glob
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 17.05.12 21:57
 */
class XmlDatabase(source: ContentResource) extends PubletAuth with Logging {
  private val prettyPrinter = new PrettyPrinter(90, 2)

  private var lastLoaded: Long = -1
  protected var users: Set[User] = Set()
  protected var repositories:Set[RepositoryModel] = Set()
  protected var permissions:Set[PermissionModel] = Set()
  protected var resourceConstraints: List[ResourceConstraint] = List()

  load()

  /**Reloads database from file, if it has been modified
   * since.
   */
  protected def load() {
    info("LOADING XML permissions file...")
    val lastMod = source.lastModification.getOrElse(-1L)
    if (lastMod > lastLoaded) {
      lastLoaded = lastMod
      val rootElem = XML.load(source.inputStream)
      users = (rootElem \ "users" \ "user").map(User(_)).toSet
      repositories = (rootElem \ "repositories" \ "repository").map(RepositoryModel(_)).toSet
      permissions = (rootElem \ "permissions" \ "grant").map(PermissionModel(_)).toSet
      resourceConstraints = (rootElem \ "resourceConstraints" \ "pattern").map(ResourceConstraint(_)).toList
    }
  }

  private def write() {
    source match {
      case ws: Writeable => synchronized {
        load()
        val bin = new ByteArrayInputStream(prettyPrinter.format(toXml).getBytes("UTF-8"))
        ws.writeFrom(bin, Some("Permission update"))
        lastLoaded = source.lastModification.getOrElse(-1L)
      }
      case _ =>
    }
  }

  private def toXml = {
    <publetAuth>
      <users>
        { users.map(_.toXml) }
      </users>
      <repositories>
        { repositories.map(_.toXml) }
      </repositories>
      <permissions>
        { permissions.map(_.toXml) }
      </permissions>
      <resourceConstraints>
        { resourceConstraints.map(_.toXml) }
      </resourceConstraints>
    </publetAuth>
  }

  /**
   * Either replaces any existing user (with same login)
   * with the given one, or adds it to the list of users.
   *
   * @param user
   */
  def updateUser(user: User) {
    synchronized {
      val newList = (users - user) + user
      this.users = newList
      write()
    }
  }

  def updateRepository(repo: RepositoryModel) {
    synchronized {
      val newList = (repositories - repo) + repo
      this.repositories = newList
      write()
    }
  }

  def updatePermission(perm: PermissionModel) {
    synchronized {
      val newList = permissions + perm
      this.permissions = newList
      write()
    }
  }


  def addResourceConstraint(rc: ResourceConstraint) {
    synchronized {
      val newList = rc :: resourceConstraints
      this.resourceConstraints = newList
      write()
    }
  }


  def getResourceConstraints(uri: String) = {
    resourceConstraints.find( rc => {
      val glob = Glob(rc.uriPattern)
      glob.matches(uri)
    })
  }

  def findUser(login: String) = users.find(_.login == login)

  def findRepository(name: String) = repositories.find(_.name == name)

  def getAllUser = users.toSeq

  def getAllRepositories = repositories.toSeq

  def getAllPermissions = permissions

  def getPolicy(login: String) = {
    val user = findUser(login).get
    getPolicy(user)
  }

  def getPolicy(user: User) = new Policy {
    def getRoles = user.groups
    def getPermissions = permissions
      .filter(!_.roles.toSet.intersect(user.groups).isEmpty)
      .flatMap(_.toPermissions.values)
      .flatten
      .map(_.permString)
      .toSet
  }
}
