package org.eknet.publet.sec

import org.eknet.publet.Path

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.04.12 23:36
 */
trait AuthManager {

  def getUser(name: String): Option[User]

  def policyFor(username: String): Option[Policy]

}

trait Policy {

  def hasPerm(resource: Path, perm: Set[String]): Boolean

  def permissions: Set[String]
}
