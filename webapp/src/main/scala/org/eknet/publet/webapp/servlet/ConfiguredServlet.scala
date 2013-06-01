package org.eknet.publet.webapp.servlet

import javax.servlet.{ServletContext, Servlet}
import org.eknet.publet.webapp.PubletWebSettings

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.05.13 16:25
 */
final class ConfiguredServlet(delegate: Servlet, sc: () => ServletContext, settings: PubletWebSettings, val config: Map[String, String] = Map.empty) {

  private[servlet] lazy val servlet = {
    delegate.init(new SimpleServletConfig(delegate.getClass.getSimpleName, sc(), config))
    delegate
  }

  def withConfig(params: Map[String, String]): ConfiguredServlet = {
    new ConfiguredServlet(delegate, sc, settings, params)
  }
  def withConfig(params: (String, String)*): ConfiguredServlet = {
    withConfig(params.toMap)
  }

  private[servlet] def initRequestSettings = RequestSettings(
    bindAddress = settings.config.getString("publet.server.bindAddress"),
    bindPort = settings.config.getInt("publet.server.port")
  )
}

case class ServletWithSettings(cs: ConfiguredServlet, settings: RequestSettings) {
  def withRemoteUser(user: String) = ServletWithSettings(cs, settings.copy(remoteUser = Some(user)))
  def withServletPath(path: String) = ServletWithSettings(cs, settings.copy(servletPath = path))
}

object ServletWithSettings {

  implicit def apply(cs: ConfiguredServlet): ServletWithSettings = new ServletWithSettings(cs, cs.initRequestSettings)

}