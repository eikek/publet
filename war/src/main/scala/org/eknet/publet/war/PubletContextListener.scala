package org.eknet.publet.war

import javax.servlet.{ServletContextEvent, ServletContextListener}
import org.eknet.publet.web.PubletWeb
import org.eknet.publet.ext.ExtWebExtension
import org.eknet.publet.webeditor.EditorWebExtension


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 13:01
 */
class PubletContextListener extends ServletContextListener {

  val extensions = List(
    new ExtWebExtension,
    new EditorWebExtension
  )

  def contextInitialized(sce: ServletContextEvent) {
    PubletWeb.initialize(sce.getServletContext)
    extensions.foreach(_.onStartup())
  }

  def contextDestroyed(sce: ServletContextEvent) {
    PubletWeb.destroy(sce.getServletContext)
  }
}
