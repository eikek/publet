package org.eknet.publet.web

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.05.12 19:20
 */
object Method extends Enumeration {

  val options = Value("OPTIONS")
  val head = Value("HEAD")
  val get = Value("GET")
  val post = Value("POST")
  val delete = Value("DELETE")

}
