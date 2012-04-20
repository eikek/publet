package org.eknet.publet.web

import javax.servlet.GenericServlet._
import java.io.File
import javax.servlet.{ServletContext, ServletContextEvent, ServletContextListener}
import org.eknet.publet.partition.git.GitPartition


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 20.04.12 14:06
 *
 */
class GitPartitionClose extends ServletContextListener {
  def contextInitialized(sce: ServletContextEvent) {
  }

  def contextDestroyed(sce: ServletContextEvent) {
    def attr(name: String) = sce.getServletContext.getAttribute(name)
    Option(attr(PubletFactory.gitpartitionKey.name)) foreach({ _.asInstanceOf[GitPartition].close() })
  }

}