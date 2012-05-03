package org.eknet.publet.auth

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.05.12 10:11
 */
class UserRuleSuite extends FunSuite with ShouldMatchers {

  test ("create user with password and algorithm") {
    var u = User("jdoe", "", "", Set(), "{MD5}sadasdasdasdasd".toCharArray)
    var alg = u.algorithmPassword
    alg._1 should be (Some("MD5"))
    alg._2 should be ("sadasdasdasdasd".toCharArray)

    u = User("jdoe", "", "", Set(), "{SHA1}sadasdasdasdasd".toCharArray)
    alg = u.algorithmPassword
    alg._1 should be (Some("SHA1"))
    alg._2 should be ("sadasdasdasdasd".toCharArray)

    u = User("jdoe", "", "", Set(), "{sha256}sadasdasdasdasd".toCharArray)
    alg = u.algorithmPassword
    alg._1 should be (Some("sha256"))
    alg._2 should be ("sadasdasdasdasd".toCharArray)
  }

  test ("create user without algorithm") {
    var u = User("jdoe", "", "", Set(), "{}sadasdasdasdasd".toCharArray)
    var alg = u.algorithmPassword
    alg._1 should be (None)
    alg._2 should be ("sadasdasdasdasd".toCharArray)
  }
}
