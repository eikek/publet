package org.eknet.publet.web

import javax.servlet.{ServletContext, ServletContextEvent, ServletContextListener}
import org.eknet.publet.partition.git.GitPartition
import PubletFactory.gitpartitionKey

/**
 * "starts" a git partition by initializing the repositories
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 20.04.12 14:06
 *
 */
class GitPartitionStarter extends ServletContextListener {
  def contextInitialized(sce: ServletContextEvent) {
    sce.getServletContext
      .setAttribute(gitpartitionKey.name, gitpartitionKey.init.get())
  }

  def contextDestroyed(sce: ServletContextEvent) {
    def attr(name: String) = sce.getServletContext.getAttribute(name)
    Option(attr(gitpartitionKey.name)) foreach { _.asInstanceOf[GitPartition].close() }
  }
}