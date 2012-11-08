package org.eknet.publet.gitr.webui.scripts

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web.util.PubletWeb

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 17.06.12 01:19
 */
class GetLogins extends ScalaScript {
  def serve() = {
    Security.checkAuthenticated()
    import ScalaScript._
    makeJson(PubletWeb.authManager.allUser.map(_.login).toList)
  }
}
