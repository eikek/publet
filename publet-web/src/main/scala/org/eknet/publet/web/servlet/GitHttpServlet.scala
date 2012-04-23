package org.eknet.publet.web.servlet

import org.eclipse.jgit.http.server.glue.MetaServlet
import org.eclipse.jgit.http.server.GitFilter
import org.eclipse.jgit.transport.resolver.FileResolver
import org.eknet.publet.web.Config
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.slf4j.LoggerFactory
import javax.servlet._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.04.12 21:03
 */
class GitHttpServlet extends MetaServlet(new GitFilter()) {
  private val log = LoggerFactory.getLogger(getClass)

  private val gitFilter = getDelegateFilter.asInstanceOf[GitFilter]
  gitFilter.setRepositoryResolver(new FileResolver[HttpServletRequest](Config.contentRoot, false))

  override def init(config: ServletConfig) {
    val fc = new FilterConfig {

      def getInitParameterNames = config.getInitParameterNames

      def getFilterName = gitFilter.getClass.getName

      def getServletContext = config.getServletContext

      def getInitParameter(name: String) = config.getInitParameter(name)
    }
    gitFilter.init(fc)
  }

  override def service(req: HttpServletRequest, res: HttpServletResponse) {
    try {
      super.service(req, res)
    } catch {
      case e:Throwable => log.error("Error in git servlet!", e); throw e;
    }
  }

}
