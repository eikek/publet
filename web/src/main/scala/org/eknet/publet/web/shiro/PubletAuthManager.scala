package org.eknet.publet.web.shiro

import org.eknet.publet.vfs.{Path, ContentResource}
import org.eknet.publet.web.Config
import org.eknet.publet.auth.{FileAuthManager, AuthManager}
import org.eknet.publet.{Includes, Publet}
import org.eknet.publet.vfs.fs.FileResource

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

  private def usersResource = allIncludesFile("users.txt").map(_.asInstanceOf[ContentResource])
  private def rulesResource = allIncludesFile("rules.txt").map(_.asInstanceOf[ContentResource])

  private def configUsers = configFile("users.txt")
  private def configRules = configFile("rules.txt")

  def delegate:Option[AuthManager] = {
    if (active) {
      val users = configUsers.map(List(_)).getOrElse(List()) :::
        (if (repositoryUsersAllowed) usersResource.map(List(_)).getOrElse(List()) else List())

      val rules = configRules.map(List(_)).getOrElse(List()) :::
        (if (repositoryUsersAllowed) rulesResource.map(List(_)).getOrElse(List()) else List())

      Some(new FileAuthManager(users, rules))
    } else {
      None
    }
  }

  def active = configRules.isDefined || configUsers.isDefined ||
    repositoryUsersAllowed && (usersResource.isDefined || rulesResource.isDefined)

  def getUser(name: String) = {
    delegate flatMap {
      am => am.getUser(name)
    }
  }

  def policyFor(username: String) = {
    delegate flatMap {
      am => am.policyFor(username)
    }
  }
}
