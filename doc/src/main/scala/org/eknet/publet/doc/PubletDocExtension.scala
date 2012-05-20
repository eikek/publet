package org.eknet.publet.doc

import org.eknet.publet.vfs.util.ClasspathContainer
import org.eknet.publet.web.{PubletWeb, WebExtension}
import org.eknet.publet.vfs.Path

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.05.12 22:29
 */
class PubletDocExtension extends WebExtension {

  def onStartup() {
    val cont = new ClasspathContainer(classOf[PubletDocExtension], None)
    PubletWeb.publet.mountManager.mount(Path("/publet/doc"), cont)
  }

  def onShutdown() {}
}
