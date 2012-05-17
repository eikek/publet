package org.eknet.publet.web.filter

import javax.servlet._
import grizzled.slf4j.Logging
import org.eknet.publet.web.{GitAction, PubletWebContext}
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.auth.RepositoryTag
import org.apache.shiro.UnavailableSecurityManagerException
import org.apache.shiro.authz.{UnauthorizedException, UnauthenticatedException}

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
      val repoModel = utils.getRepositoryModel
      val gitAction = utils.getGitAction.getOrElse(GitAction.pull)
      repoModel.foreach { repo =>
        Security.checkGitAction(gitAction, repo)
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
        info("Unauthenticated user. Redirect to login")
        PubletWebContext.redirectToLoginPage()
      }
    }
  }

  def init(filterConfig: FilterConfig) {}

  def destroy() {}
}
