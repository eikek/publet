package org.eknet.publet.war

import javax.servlet.{ServletContextEvent, ServletContextListener}
import org.eknet.publet.webeditor.EditorWebExtension


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 20:57
 */
class WebEditorListener extends ServletContextListener with PubletRetriever {
  def contextInitialized(sce: ServletContextEvent) {
    val publet = webPublet(sce)
    new EditorWebExtension().onStartup(publet, sce.getServletContext)
  }

  def contextDestroyed(p1: ServletContextEvent) {

  }
}
