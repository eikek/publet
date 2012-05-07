package org.eknet.publet.vfs

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 07.05.12 12:35
 *
 */
class PathSuite extends FunSuite with ShouldMatchers {

  test ("rebase simple paths") {
    val p0 = Path("/a/b/c/d.html")
    val p1 = Path("/a/b/x/y/z.html")
    val reb = p0.rebase(p1)
    reb.asString should equal ("../../c/d.html")
  }
}
