package org.eknet.publet.war

import org.eknet.publet.web.{WebPublet, WebContext}
import javax.servlet.ServletContextEvent

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 21:00
 */
trait PubletRetriever {

  def webPublet(sce: ServletContextEvent) =
    sce.getServletContext.getAttribute(WebContext.webPubletKey.name).asInstanceOf[WebPublet]
}
