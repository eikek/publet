package org.eknet.publet.web.scripts

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web.WebContext
import ScalaScript._
import org.eknet.publet.vfs.{Path, ContentType}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.05.12 20:49
 */
object Logout extends ScalaScript {
  def actionUrl(page:String) = WebContext.rebasePath(Path("/publet/scripts/"+page+".html"))

  def serve() = {
    val path = WebContext().requestPath
    if (Security.isAuthenticated) {
      Security.subject.logout()
      if (path.name.targetType == ContentType.json) {
        makeJson(Map("success"->true, "message"->"Logged out."))
      } else {
        Login.serve()
      }
    } else {
      Login.serve()
    }
  }
}
