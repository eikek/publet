package org.eknet.publet.web.servlet

import org.eclipse.jgit.http.server.glue.MetaServlet
import org.eclipse.jgit.http.server.GitFilter
import javax.servlet.{ServletConfig, FilterConfig}
import javax.servlet.http.HttpServletRequest
import org.eclipse.jgit.transport.resolver.FileResolver
import org.eknet.publet.web.Config

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.04.12 21:03
 */
class GitHttpServlet extends MetaServlet(new GitFilter()) {

  private val gitFilter = getDelegateFilter.asInstanceOf[GitFilter]
  gitFilter.setRepositoryResolver(new FileResolver[HttpServletRequest](Config.contentRoot, false))

  override def init(config: ServletConfig) {
    gitFilter.init(new FilterConfig {

      def getInitParameterNames = config.getInitParameterNames

      def getFilterName = gitFilter.getClass.getName

      def getServletContext = config.getServletContext

      def getInitParameter(name: String) = config.getInitParameter(name)
    })
  }
}
