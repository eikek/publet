package org.eknet.publet.web

import org.eclipse.jgit.http.server.glue.MetaServlet
import org.eclipse.jgit.http.server.{GitFilter, GitServlet}
import javax.servlet.{ServletConfig, FilterConfig}
import java.util.Enumeration
import javax.servlet.http.HttpServletRequest
import org.eclipse.jgit.transport.resolver.FileResolver

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.04.12 21:03
 */
class GitHttpServlet extends MetaServlet(new GitFilter()) {

  private val gitFilter = getDelegateFilter.asInstanceOf[GitFilter]
  gitFilter.setRepositoryResolver(new FileResolver[HttpServletRequest](Config.contentRoot, false))

  override def init(config: ServletConfig) {
    gitFilter.init(new FilterConfig {

      def getInitParameterNames = new Enumeration[String] {
        val set = Config.keySet.filter(_.startsWith("git.")).toIterator
        def nextElement() = set.next()
        def hasMoreElements = set.hasNext
      }

      def getFilterName = gitFilter.getClass.getName

      def getServletContext = config.getServletContext

      def getInitParameter(name: String) = Config.value("git."+ name).getOrElse(null)
    })
  }
}
