package org.eknet.publet.web.template

import org.eknet.publet.web.scripts.{Logout, Login, WebScriptResource}
import org.eknet.publet.vfs.{Path, ResourceName}
import org.eknet.publet.web.{PubletWeb, WebExtension}
import org.eknet.publet.vfs.util.{ClasspathContainer, MapContainer}
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
      new ClasspathContainer(classOf[BootstrapTemplate], Some(Path("../includes/bootstrap"))))

    val layoutUrl = classOf[BootstrapTemplate].getResource("../includes/bootstrap/bootstrap.single.jade")
    PubletWeb.scalateEngine.urlResources.addUrl(layoutUrl)
    PubletWeb.scalateEngine.setDefaultLayoutUri(layoutUrl.toString)
  }

  def onShutdown() {

  }
}
