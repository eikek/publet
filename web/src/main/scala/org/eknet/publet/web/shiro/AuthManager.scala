package org.eknet.publet.web.shiro

import org.eknet.publet.Includes
import org.eknet.publet.vfs.{ContentResource, Path}
import org.eknet.publet.web.{Config, PubletWeb}
import org.eknet.publet.auth.xml.{PermissionModel, XmlDatabase}
import org.eknet.publet.auth._
import org.xml.sax.SAXParseException
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 15.05.12 23:49
 */
class AuthManager extends PubletAuth with Logging {

  private def getPermissionXml = {
    val permissionPath = Path(Includes.allIncludes+"config/permissions.xml").toAbsolute
    PubletWeb.contentRoot
      .lookup(permissionPath)
      .collect({case c:ContentResource=>c})
  }

  private def delegate = try {
    getPermissionXml.map(r => new XmlDatabase(r)).getOrElse(SuperUserAuth)
  } catch {
    case e: SAXParseException => {
      error("Error parsing permission xml. Fallback to superuser realm!", e)
      SuperUserAuth
    }
  }

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
}

private object SuperUserAuth extends PubletAuth {
  private val superuser = User("superadmin",
    "superadmin".toCharArray,
    None,
    Set("superadmin"),
    Map(UserProperty.fullName.toString -> "Publet Superadmin"))

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
}
