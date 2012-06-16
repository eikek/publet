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

import org.eknet.publet.vfs.{ContentResource, Path}
import org.eknet.publet.web.{Config, PubletWeb}
import org.eknet.publet.auth.xml.{PermissionModel, XmlDatabase}
import org.eknet.publet.auth._
import org.xml.sax.SAXParseException
import grizzled.slf4j.Logging
import org.eknet.publet.Publet

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 15.05.12 23:49
 */
class AuthManager extends PubletAuth with Logging {

  private def getPermissionXml = {
    val permissionPath = Path(Publet.allIncludes+"config/permissions.xml").toAbsolute
    PubletWeb.contentRoot
      .lookup(permissionPath)
      .collect({case c:ContentResource=>c})
  }

  @volatile
  private var database: Option[PubletAuth] = None

  private def delegate: PubletAuth = {
    database.getOrElse {
      val db = try {
        getPermissionXml.map(r => new XmlDatabase(r)).getOrElse(SuperUserAuth)
      } catch {
        case e: SAXParseException => {
          error("Error parsing permission xml. Fallback to superuser realm!", e)
          SuperUserAuth
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
  def findUser(login: String) = delegate.findUser(login)
  def getPolicy(login: String) = delegate.getPolicy(login)
  def getPolicy(user: User) = delegate.getPolicy(user)
  def updateUser(user: User) {
    delegate.updateUser(user)
  }
  def updateRepository(repo: RepositoryModel) {
    delegate.updateRepository(repo)
  }
  def updatePermission(perm: PermissionModel) {
    delegate.updatePermission(perm)
  }
  def removePermission(group: String, perm: Permission) {
    delegate.removePermission(group, perm);
  }
  def addResourceConstraint(rc: ResourceConstraint) {
    delegate.addResourceConstraint(rc)
  }
}

private object SuperUserAuth extends PubletAuth {
  private def superuser = User("superadmin",
    Config("superadminPassword").getOrElse("superadmin").toCharArray,
    None,
    Set("superadmin"),
    Map(UserProperty.fullName.toString -> "Publet Superadmin"))


  def getResourceConstraints(uri: String) = None
  def getAllRepositories = List()
  def getAllUser = List(superuser)
  def findUser(login: String) = getAllUser.find(_.login==login)
  def getPolicy(login: String) = findUser(login).map(getPolicy).getOrElse(Policy.Empty)
  def getPolicy(user: User) = new Policy {
    def getRoles = user.groups
    def getPermissions = Set("*")
  }
  def updateUser(user: User) {}
  def updateRepository(repo: RepositoryModel) {}
  def updatePermission(perm: PermissionModel) {}
  def removePermission(group: String, perm: Permission) {}
  def addResourceConstraint(rc: ResourceConstraint) {}
}
