package org.eknet.publet.web.filter

import javax.servlet._
import grizzled.slf4j.Logging
import org.eknet.publet.web.shiro.Security
import org.apache.shiro.authz.{UnauthorizedException, UnauthenticatedException}
import org.eknet.publet.web.{PubletWeb, GitAction, PubletWebContext}

/**
 * Does default authorization checks.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 20:49
 */
class AuthzFilter extends Filter with HttpFilter with Logging with PageWriter {

  def doFilter(req: ServletRequest, resp: ServletResponse, chain: FilterChain) {
    val utils = getRequestUtils(req)
    try {
      if (!PubletShiroFilter.anonRequestAllowed) {
        val repoModel = utils.getRepositoryModel
        val gitAction = utils.getGitAction.getOrElse(GitAction.pull)

        repoModel.foreach { repo =>
          Security.checkGitAction(gitAction, repo)
        }
        PubletWeb.authManager.getResourceConstraints(utils.requestUri)
          .filterNot(_.perm.isAnon)
          .foreach(rc => Security.checkPerm(rc.perm.permString))
      }

      chain.doFilter(req, resp)

    } catch {
      case uae: UnauthorizedException => {
        error("Unauthorized: "+ uae.getLocalizedMessage)
        writeUnauthorizedError(resp)
      }
      case se: ServletException if (se.getCause.isInstanceOf[UnauthorizedException]) => {
        error("Unauthorized: "+ se.getCause.getLocalizedMessage)
        writeUnauthorizedError(resp)
      }
      case ue: UnauthenticatedException => {
        info("Unauthenticated user for '"+req.getRequestURI+"'. Redirect to login")
        PubletWebContext.redirectToLoginPage()
      }
    }
  }

  def init(filterConfig: FilterConfig) {}

  def destroy() {}
}
