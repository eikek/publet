package org.eknet.publet.web.servlet

import org.eclipse.jgit.http.server.glue.MetaServlet
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.slf4j.LoggerFactory
import javax.servlet._
import grizzled.slf4j.Logging
import org.eclipse.jgit.http.server.{GitSmartHttpTools, GitFilter}
import org.eclipse.jgit.lib.Constants
import org.eknet.publet.web.{WebPublet, Config}
import org.eknet.publet.vfs.Path
import org.eknet.publet.gitr.RepositoryName
import org.eknet.publet.web.shiro.Security
import org.eclipse.jgit.transport.resolver.{ServiceNotAuthorizedException, FileResolver}

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

  def getRepositoryName(req:HttpServletRequest): RepositoryName = {
    val repopath = Path(GitSmartHttpTools.stripServiceSuffix(req.getRequestURI)).strip
    RepositoryName(repopath.withExt("").asString)
  }

  def permissionCheck(req:HttpServletRequest): Boolean = {
    val reponame = getRepositoryName(req)
    if (GitSmartHttpTools.isUploadPack(req)) {
      //clone/pull
      return Security.hasPerm(Security.gitRead, reponame.path)
    }
    if (req.getRequestURI.endsWith("/"+ Constants.INFO_REFS)) {
      //fetch
      return Security.hasPerm(Security.gitRead, reponame.path)
    }
    if (GitSmartHttpTools.isReceivePack(req)) {
      //push
      return Security.hasPerm(Security.gitWrite, reponame.path)
    }
    true
  }
  override def service(req: HttpServletRequest, res: HttpServletResponse) {
    try {
      if (!permissionCheck(req)) {
        throw new ServiceNotAuthorizedException()
      } else {
        super.service(req, res)
        if (GitSmartHttpTools.isReceivePack(req)) {
          val publ = WebPublet()
          info("Updating git workspace...")
          val reponame = getRepositoryName(req)
          publ.gitPartMan.get(reponame.path).foreach(_.updateWorkspace())
        }
      }
    } catch {
      case e:Throwable => log.error("Error in git servlet!", e); throw e;
    }
  }

}
