package org.eknet.publet.web.template

import org.eknet.publet.vfs.Path
import org.eknet.publet.web.{PubletWeb, WebExtension}
import org.eknet.publet.vfs.util.ClasspathContainer
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 18:25
 */
class BootstrapTemplate extends WebExtension with Logging {

  def onStartup() {
    info("Installing bootstrap template")
    val publ = PubletWeb.publet
    Templates.mountJQuery(publ)
    Templates.mountHighlightJs(publ)
    publ.mountManager.mount(Path("/publet/bootstrap/"),
      new ClasspathContainer(base = "/org/eknet/publet/web/includes/bootstrap"))

    PubletWeb.scalateEngine.setDefaultLayoutUri("/publet/bootstrap/bootstrap.single.jade")
  }

  def onShutdown() {

  }
}
