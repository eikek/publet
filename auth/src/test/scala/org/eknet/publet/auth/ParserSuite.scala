package org.eknet.publet.auth

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.05.12 10:01
 */
class ParserSuite extends FunSuite with ShouldMatchers {

  val user0 = "jdoe:John Doe:jdoe@email.com:editor:{}secret"
  val user1 = "jdoe2:John Second Doe:jdoe2@email.com:editor:{MD5}5ebe2294ecd0e0f08eab7690d2a6ee69"
  val user2 = "jdoe3:John Third Doe:jdoe3@email.com:editor:{SHA256}2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b"

  val parser = new AuthParser

  test ("parse sample users") {
    var u = parser.parsePrincipal(user0).get
    u.login should be ("jdoe")
    u.email should be ("jdoe@email.com")
    u.hasRole("editor") should be (true)
    u.algorithmPassword._1 should be (None)
    u.algorithmPassword._2 should be ("secret".toCharArray)

    u = parser.parsePrincipal(user1).get
    u.login should be ("jdoe2")
    u.email should be ("jdoe2@email.com")
    u.hasRole("editor") should be (true)
    u.algorithmPassword._1 should be (Some("MD5"))
    u.algorithmPassword._2 should be ("5ebe2294ecd0e0f08eab7690d2a6ee69".toCharArray)
  }

  test ("parse sample permission rules") {
    var r = parser.parsePermission("manager = get:*,put:main:*,git:*").get
    r.roles should be (Set("manager"))
    r.perms should have size 3
    r.perms should contain ("get:*")
    r.perms should contain ("put:main:*")
    r.perms should contain ("git:*")

    r = parser.parsePermission("manager,editor = get:*,put:main:*,git:read").get
    r.roles should be (Set("manager", "editor"))
    r.perms should have size 3
    r.perms should contain ("get:*")
    r.perms should contain ("put:main:*")
    r.perms should contain ("git:read")
  }

  test ("parse simple url mappings") {
    var u = parser.parseUrlMapping("/main/index.html = anon").get
    u._1 should equal ("/main/index.html")
    u._2 should equal ("anon")

    u = parser.parseUrlMapping("/main/** = auth").get
    u._1 should equal ("/main/**")
    u._2 should equal ("auth")

    u = parser.parseUrlMapping("/publet/scripts/** = auth").get
    u._1 should equal ("/publet/scripts/**")
    u._2 should equal ("auth")
  }
}
