package org.eknet.publet.web.servlet

import org.eclipse.jgit.http.server.glue.MetaServlet
import org.eclipse.jgit.transport.resolver.FileResolver
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.slf4j.LoggerFactory
import javax.servlet._
import grizzled.slf4j.Logging
import org.eclipse.jgit.http.server.{GitSmartHttpTools, GitFilter}
import org.apache.shiro.subject.Subject
import org.apache.shiro.SecurityUtils
import org.eclipse.jgit.lib.Constants
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web.{PubletFactory, WebPublet, WebContext, Config}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.04.12 21:03
 */
class GitHttpServlet extends MetaServlet(new GitFilter()) with Logging {
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
      val subj = SecurityUtils.getSubject
      if (GitSmartHttpTools.isUploadPack(req)) {
        //clone/pull
        subj.checkPermission(Security.gitRead)
      }
      if (req.getRequestURI.endsWith("/"+ Constants.INFO_REFS)) {
        //fetch
        subj.checkPermission(Security.gitRead)
      }
      if (GitSmartHttpTools.isReceivePack(req)) {
        //push
        subj.checkPermission(Security.gitWrite)
      }
      super.service(req, res)
      if (GitSmartHttpTools.isReceivePack(req)) {
        val publ = WebPublet()
        info("Updating git workspace...")
        publ.gitPartMan.get(PubletFactory.mainRepoName.path).foreach(_.updateWorkspace())
      }

    } catch {
      case e:Throwable => log.error("Error in git servlet!", e); throw e;
    }
  }

}
