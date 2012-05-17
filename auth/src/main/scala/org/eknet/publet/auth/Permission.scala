package org.eknet.publet.auth

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 17:20
 */
case class Permission(perm: String, repository: Option[String]) {

  def permString = perm + repository.map(":"+_).getOrElse("")

}
