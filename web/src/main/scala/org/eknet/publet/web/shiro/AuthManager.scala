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

package org.eknet.publet.web.shiro

import org.eknet.publet.vfs.{Container, ContentResource, Path}
import org.eknet.publet.auth.xml.{PermissionModel, XmlDatabase}
import org.eknet.publet.auth._
import org.xml.sax.SAXParseException
import grizzled.slf4j.Logging
import org.eknet.publet.Publet
import com.google.inject.{Inject, Singleton}
import org.eknet.publet.vfs.events.ContentWrittenEvent
import com.google.common.eventbus.Subscribe
import org.eknet.publet.web.filter.PostReceiveEvent
import com.google.inject.name.Named
import org.eknet.publet.web.Config

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 15.05.12 23:49
 */
@Singleton
class AuthManager @Inject() (@Named("contentroot") contentRoot: Container, config: Config) extends PubletAuth with Logging {

  private var lastModification: Option[Long] = None

  private def getPermissionXml = {
    val permissionPath = Path(Publet.allIncludes+"config/permissions.xml").toAbsolute
    contentRoot
      .lookup(permissionPath)
      .collect({case c:ContentResource=>c})
  }

  @Subscribe
  def reloadOnChange(event: ContentWrittenEvent) {
    reloadIfChanged()
  }

  @Subscribe
  def reloadOnPush(event: PostReceiveEvent) {
    reloadIfChanged()
  }

  def reloadIfChanged() {
    getPermissionXml map { newFile =>
      if (lastModification.getOrElse(0L) != newFile.lastModification.getOrElse(0L)) {
        info("Reloading permissions due to file change")
        reload()
      }
    }
  }

  @volatile
  private var database: Option[PubletAuth] = None

  private def delegate: PubletAuth = {
    database.getOrElse {
      val db = try {
        getPermissionXml.map(r => { lastModification = r.lastModification; new XmlDatabase(r)}).getOrElse {
          val msg = "No permission.xml file found."
          config("superadminEnabled").map(_.toBoolean) match {
            case Some(false) => warn(msg + " Super-user account disabled."); PubletAuth.Empty
            case _ => warn(msg + " Falling back to super-user authentication!"); new SuperUserAuth(config)
          }
        }
      } catch {
        case e: SAXParseException => {
          val msg = "Error parsing permission xml."
          config("superadminEnabled").map(_.toBoolean) match {
            case Some(false) => error(msg + " Super-user account disabled."); PubletAuth.Empty
            case _ => error(msg + " Falling back to super-user authentication!"); new SuperUserAuth(config)
          }
        }
      }
      this.database = Some(db)

      delegate
    }
  }

  def reload() {
    database = None
  }

  def getResourceConstraints(uri: String) = delegate.getResourceConstraints(uri)
  def getAllRepositories = delegate.getAllRepositories
  def getAllUser = delegate.getAllUser
  def getAllGroups = delegate.getAllGroups
  def findUser(login: String) = delegate.findUser(login)
  def getAllPermissions = delegate.getAllPermissions
  def getPolicy(login: String) = delegate.getPolicy(login)
  def getPolicy(user: User) = delegate.getPolicy(user)
  def updateUser(user: User) {
    delegate.updateUser(user)
    reload()
  }
  def updateRepository(repo: RepositoryModel) {
    delegate.updateRepository(repo)
    reload()
  }
  def removeRepository(repoName: String) {
    delegate.removeRepository(repoName)
    reload()
  }
  def updatePermission(perm: PermissionModel) {
    delegate.updatePermission(perm)
    reload()
  }
  def removePermission(group: String, perm: Permission) {
    delegate.removePermission(group, perm)
    reload()
  }
  def addResourceConstraint(rc: ResourceConstraint) {
    delegate.addResourceConstraint(rc)
    reload()
  }
}

private class SuperUserAuth(config: Config) extends PubletAuth {
  private def superuser = User("superadmin",
    config("superadminPassword").getOrElse("superadmin").toCharArray,
    None,
    Array(),
    Set("superadmin"),
    Map(UserProperty.fullName.toString -> "Publet Superadmin"))


  def getResourceConstraints(uri: String) = None
  def getAllRepositories = List()
  def getAllUser = List(superuser)
  def getAllGroups = superuser.groups
  def findUser(login: String) = getAllUser.find(_.login==login)
  def getAllPermissions = Set(PermissionModel.allPermission)
  def getPolicy(login: String) = findUser(login).map(getPolicy).getOrElse(Policy.Empty)
  def getPolicy(user: User) = new Policy {
    def getRoles = user.groups
    def getPermissions = Set("*")
  }
  def updateUser(user: User) {}
  def updateRepository(repo: RepositoryModel) {}
  def removeRepository(repoName: String) {}
  def updatePermission(perm: PermissionModel) {}
  def removePermission(group: String, perm: Permission) {}
  def addResourceConstraint(rc: ResourceConstraint) {}
}
