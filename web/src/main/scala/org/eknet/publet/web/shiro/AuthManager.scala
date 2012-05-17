package org.eknet.publet.web.shiro

import org.eknet.publet.Includes
import org.eknet.publet.vfs.{ContentResource, Path}
import org.eknet.publet.web.{Config, PubletWeb}
import org.eknet.publet.auth.xml.{PermissionModel, XmlDatabase}
import org.eknet.publet.auth.{RepositoryModel, User, PubletAuth}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 15.05.12 23:49
 */
class AuthManager extends PubletAuth {

  private def delegate = {
    val permissionPath = Path(Includes.allIncludes+"config/permissions.xml").toAbsolute
    val xml = PubletWeb.contentRoot
      .lookup(permissionPath)
      .collect({case c:ContentResource=>c})
    xml.map(r => new XmlDatabase(r)).getOrElse(PubletAuth.Empty)
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
