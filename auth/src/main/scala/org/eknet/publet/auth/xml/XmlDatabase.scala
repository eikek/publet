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
import org.eknet.publet.vfs.{ChangeInfo, Writeable, ContentResource}
import java.io.ByteArrayInputStream
import org.eknet.publet.auth._
import grizzled.slf4j.Logging
import org.eknet.publet.Glob
import org.apache.shiro.{ShiroException, SecurityUtils}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 17.05.12 21:57
 */
class XmlDatabase(source: ContentResource, passwServiceProvider: PasswordServiceProvider, realmNameFun: Option[() => String]) extends PubletAuth with Logging {
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

  private def write(message: String) {
    source match {
      case ws: Writeable => synchronized {
        load()
        val bin = new ByteArrayInputStream(prettyPrinter.format(toXml).getBytes("UTF-8"))
        val user = getCurrentUser.map { u =>
          new ChangeInfo(u.getProperty(UserProperty.fullName), u.getProperty(UserProperty.email), message) }
        ws.writeFrom(bin, user)
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

  private def getCurrentUser = {
    try {
      Option(SecurityUtils.getSubject.getPrincipal) flatMap { p =>
        if (p != null) findUser(p.toString)
        else None
      }
    } catch {
      case e: ShiroException => None
    }
  }

  def updateUser(user: User) {
    synchronized {
      val newList = (users - user) + user
      this.users = newList
      write("Permission: update user "+ user.login)
    }
  }

  def setPassword(login: String, plainTextPassword: String, algorithm: Option[String]) {
    val user = findUser(login).getOrElse(sys.error("User '"+login+"' not found"))
    val realmName = realmNameFun.map(_()).getOrElse(defaultRealmName)
    val newdigest = DigestGenerator.encodePasswordInA1Format(user.login, realmName, plainTextPassword)
    val newpass = algorithm.orElse(user.algorithm)
        .map(a => passwServiceProvider.forAlgorithm(Algorithm.withName(a.toUpperCase)))
        .map(ps => ps.encryptPassword(plainTextPassword))
        .getOrElse(plainTextPassword)

    val newUser = new User(user.login, newpass.toCharArray, algorithm.orElse(user.algorithm), newdigest.toCharArray, user.groups, user.properties)
    updateUser(newUser)
  }

  val defaultRealmName = "Webdav Area"

  def updateRepository(repo: RepositoryModel) {
    synchronized {
      val newList = repositories.filter(_.name != repo.name) + repo
      this.repositories = newList
      write("Permission: Update repository " +repo.name)
    }
  }


  def removeRepository(repoName: String) {
    val name = if (repoName.endsWith(".git")) repoName.substring(0, repoName.length-4) else repoName
    synchronized {
      val newList = repositories.filterNot(_.name == name)
      this.repositories = newList
      write("Permission: Remove repository "+ name)
    }
  }

  def updatePermission(perm: PermissionModel) {
    synchronized {
      val newList = permissions + perm
      this.permissions = newList
      write("Permission: Update permission model: "+ perm)
    }
  }

  def removePermission(group: String, perm: Permission) {
    synchronized {
      def groupPermFilter(pm:PermissionModel) = pm.perm == perm.perm && pm.roles.contains(group)

      val transformed = permissions.withFilter(groupPermFilter)
        .map(pm => PermissionModel(pm.perm, pm.repository, pm.roles.filterNot(_ == group)))
        .filterNot(_.roles.isEmpty)

      val newlist =  permissions.filterNot(groupPermFilter) ++ transformed
      this.permissions = newlist
      write("Permission: Remove permission '"+perm+"' for group '"+group+"'")
    }
  }

  def addResourceConstraint(rc: ResourceConstraint) {
    synchronized {
      val newList = rc :: resourceConstraints
      this.resourceConstraints = newList
      write("Permission: Add resource constraint: "+ rc)
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

  def getAllGroups = {
    val permGroups = permissions flatMap (_.roles)
    val userGroups = users flatMap (_.groups)
    userGroups ++ permGroups
  }

  def getPolicy(login: String) = {
    val user = findUser(login).get
    getPolicy(user)
  }

  def getPolicy(user: User) = new Policy {
    def getRoles = user.groups
    def getPermissions = (standardPermissions ++ repositoryOwnerPerms)
      .map(_.permString)
      .toSet

    private def repositoryOwnerPerms = getAllRepositories
      .filter(_.owner == user.login)
      .flatMap(rm => List(Permission(GitAction.push.toString, Some(rm.name)),
          Permission(GitAction.gitadmin.toString, Some(rm.name))))
      .toSet

    private def standardPermissions = permissions
      .filter(!_.roles.toSet.intersect(user.groups).isEmpty)
      .flatMap(_.toPermissions.values)
      .flatten

    override def toString = "Policy{user="+user.login+"}"
  }
}
