package org.eknet.publet.web.shiro

import org.eknet.publet.{Path, Publet}
import org.eknet.publet.sec.{FileAuthManager, AuthManager}
import org.eknet.publet.resource.ContentResource
import org.eknet.publet.web.WebContext
import org.eknet.publet.web.WebContext._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 21:06
 */
class PubletAuthManager(publet: Publet) extends AuthManager {

  private def usersResource = publet.lookup(Path("/"+mount+"/.allIncludes/users.txt"))
  private def rulesResource = publet.lookup(Path("/"+mount+"/.allIncludes/rules.txt"))

  private def mount = WebContext(mainMount).get

  def delegate:Option[AuthManager] = {
    if (active) {
      Some(new FileAuthManager(usersResource.get.asInstanceOf[ContentResource],
        rulesResource.get.asInstanceOf[ContentResource]))
    } else {
      None
    }
  }

  def active = usersResource.isDefined && rulesResource.isDefined

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
