package org.eknet.publet.doc

import org.eknet.publet.vfs.util.ClasspathContainer
import org.eknet.publet.web.{PubletWeb, WebExtension}
import org.eknet.publet.vfs.Path
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.05.12 22:29
 */
class PubletDocExtension extends WebExtension with Logging {

  def onStartup() {
    info("Installing publet user documentation...")
    val cont = new ClasspathContainer(classOf[PubletDocExtension], None)
    PubletWeb.publet.mountManager.mount(Path("/publet/doc"), cont)
  }

  def onShutdown() {}
}
