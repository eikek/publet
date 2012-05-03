package org.eknet.publet.web.shiro

import org.eknet.publet.vfs.{Path, ContentResource}
import org.eknet.publet.{Includes, Publet}
import org.eknet.publet.vfs.fs.FileResource
import org.eknet.publet.web.{WebContext, Config}
import org.eknet.publet.auth.{User, Policy, FileAuthManager, AuthManager}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 21:06
 */
class PubletAuthManager(publet: Publet) extends AuthManager {

  private def repositoryUsersAllowed = Config("publet.auth.fromRepository").getOrElse("true").toBoolean

  private def mount = Config.mainMount

  private def allIncludesFile(filename: String) = publet.rootContainer
    .lookup((Path(mount)/ Includes.allIncludesPath /filename).toAbsolute)

  private def configFile(filename: String) = {
    val f = Config.getFile(filename)
    if (f.exists()) Some(new FileResource(f, Path(Config.directory)))
    else None
  }

  private def usersResource = allIncludesFile("auth/users.cfg").map(_.asInstanceOf[ContentResource])
  private def rulesResource = allIncludesFile("auth/permissions.cfg").map(_.asInstanceOf[ContentResource])
  private def mappingsResource = allIncludesFile("auth/url_authz.cfg").map(_.asInstanceOf[ContentResource])

  private def configUsers = configFile("users.cfg")
  private def configRules = configFile("permissions.cfg")
  private def configMappings = configFile("url_authz.cfg")

  lazy val delegate: AuthManager = {
    val users = configUsers.map(List(_)).getOrElse(List()) :::
      (if (repositoryUsersAllowed) usersResource.map(List(_)).getOrElse(List()) else List())

    val rules = configRules.map(List(_)).getOrElse(List()) :::
      (if (repositoryUsersAllowed) rulesResource.map(List(_)).getOrElse(List()) else List())

    val mappings = configMappings.map(List(_)).getOrElse(List()) :::
      (if (repositoryUsersAllowed) mappingsResource.map(List(_)).getOrElse(List()) else List())

    new FileAuthManager(users, rules, mappings)
  }

  lazy val isActive = configRules.isDefined || configUsers.isDefined ||
    (repositoryUsersAllowed && (usersResource.isDefined || rulesResource.isDefined))

  def getUser(name: String) = delegate.getUser(name)

  def policyFor(username: String) = delegate.policyFor(username)
  def policyFor(user: User) = delegate.policyFor(user)
  def urlMappings = delegate.urlMappings
}
