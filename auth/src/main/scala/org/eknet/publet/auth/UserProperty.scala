package org.eknet.publet.auth

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 14:40
 */
object UserProperty extends Enumeration {

  val fullName, email, enabled = Value

  def exists(name: String): Boolean = values.exists(_.toString == name)
}
