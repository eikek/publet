/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.publet.web.filter

import javax.servlet._
import grizzled.slf4j.Logging
import http.HttpServletResponse
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
        PubletWeb.authManager.getResourceConstraints(utils.applicationUri)
          .filterNot(_.perm.isAnon)
          .foreach(rc => Security.checkPerm(rc.perm.permString))
      }

      chain.doFilter(req, resp)

    } catch {
      case uae: UnauthorizedException => {
        error("Unauthorized: "+ uae.getLocalizedMessage)
        writeError(HttpServletResponse.SC_UNAUTHORIZED, resp)
      }
      case se: ServletException if (se.getCause.isInstanceOf[UnauthorizedException]) => {
        error("Unauthorized: "+ se.getCause.getLocalizedMessage)
        writeError(HttpServletResponse.SC_UNAUTHORIZED, resp)
      }
      case ue: UnauthenticatedException => {
        info("Unauthenticated user for '"+utils.applicationUri+"'. Redirect to login")
        PubletWebContext.redirectToLoginPage()
      }
    }
  }

  def init(filterConfig: FilterConfig) {}

  def destroy() {}
}
