package org.eknet.publet.web.scripts

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.shiro.Security
import ScalaScript._
import org.eknet.publet.web.PubletWebContext

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.05.12 20:49
 */
object Logout extends ScalaScript {

  def serve() = {
    if (Security.isAuthenticated) {
      Security.subject.logout()
    }
    PubletWebContext.param("redirect") match {
      case Some(uri) => PubletWebContext.redirect(uri)
      case _ =>
    }
    makeJson(Map("success"->true, "message"->"Logged out."))
  }
}
