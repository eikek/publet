package org.eknet.publet.web.servlet

import org.eclipse.jgit.http.server.glue.MetaServlet
import org.eknet.publet.partition.git.GitPartition
import org.eknet.publet.web.WebContext._
import org.slf4j.LoggerFactory
import javax.servlet._
import org.eknet.publet.web.{PubletFactory, Config}

/** Building with the infrastructure provided by jgit.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.04.12 23:52
 */
class PubletServlet extends MetaServlet(new PubletFilter) {
  private val log = LoggerFactory.getLogger(getClass)
  val filter = getDelegateFilter

  //this is necessary because MetaServlet is not calling super.init, thus the servletconfig is not available
  var servletConfig: ServletConfig = null

  override def init(config: ServletConfig) {
    this.servletConfig = config
    filter.init(new FilterConfig {

      def getInitParameterNames = config.getInitParameterNames

      def getFilterName = filter.getClass.getName

      def getServletContext = config.getServletContext

      def getInitParameter(name: String) = config.getInitParameter(name)
    })
    publetInit()
  }


  def publetInit() {
    synchronized {
      val gp = new GitPartition('publetroot,
        Config.contentRoot,
        "publetrepo",
        Config("git.pollInterval").getOrElse("1500").toInt)
      val publ = PubletFactory.createPublet(gp)

      servletConfig.getServletContext
        .setAttribute(gitpartitionKey.name, gp)
      servletConfig.getServletContext
        .setAttribute(publetKey.name, publ)

    }
  }

  override def destroy() {
    try {
      servletConfig.getServletContext.getAttribute(gitpartitionKey.name)
        .asInstanceOf[GitPartition].close()
    } catch {
      case e:Throwable => log.error("Error on destroy.", e)
    }
  }
}
