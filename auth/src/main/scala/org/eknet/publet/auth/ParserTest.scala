package org.eknet.publet.auth

import java.io.ByteArrayInputStream

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.04.12 22:53
 */
object ParserTest extends AuthParser {

  def main(args: Array[String]) {
    val user = "eike: manager,reporter : supersecret"
    val perm = """
      # the main rules
      '/.publets/*' : manager,guest : edit
      '/.allIncludes/*scala$' : manager : edit

    """

    val bin = new ByteArrayInputStream(perm.getBytes("UTF-8"))

    println("princpal: " + principal(user))
    println(rules(bin).foreach(println))
  }
}
