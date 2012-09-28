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

package org.eknet.publet.web.req

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eknet.publet.web.filter._
import org.eknet.publet.auth.{GitAction, RepositoryTag}
import org.eknet.publet.web.{PubletRequestWrapper, PubletWeb}
import RequestHandlerFactory._
import org.eknet.publet.web.shiro.{AuthzFilter, AuthcFilter, Security}
import org.apache.shiro.authz.{UnauthorizedException, UnauthenticatedException}

/**
 * Creates a filter chain to server git requests.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.09.12 15:18
 */
class GitHandlerFactory extends RequestHandlerFactory with PubletRequestWrapper {

  def getApplicableScore(req: HttpServletRequest) =
    if (req.isGitRequest) EXACT_MATCH else NO_MATCH

  def createFilter() = new SuperFilter(Seq(
      Filters.webContext,
      GitShiroFilter,
      Filters.exceptionHandler,
      GitAuthzFilter,
      new GitHttpFilter(PubletWeb.gitr)
    ))

  object GitShiroFilter extends AuthcFilter {
    override def isEnabled(request: HttpServletRequest) = {
      val action = request.getGitAction
      val tag = request.getRepositoryModel
        .map(_.tag)
        .getOrElse(RepositoryTag.open)

      val anon = tag == RepositoryTag.open && action.exists(_ == GitAction.pull)
      !anon
    }
  }

  object GitAuthzFilter extends AuthzFilter {
    override def checkResourceAccess(req: HttpServletRequest) {
      val gitAction = req.getGitAction.getOrElse(GitAction.pull)
      val repoModel = req.getRepositoryModel
      repoModel.foreach { repo =>
        Security.checkGitAction(gitAction, repo)
      }
    }

    override def onUnauthorized(ex: UnauthorizedException, req: HttpServletRequest, res: HttpServletResponse) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN)
    }

    override def onUnauthenticated(ex: UnauthenticatedException, req: HttpServletRequest, res: HttpServletResponse) {
      res.sendError(HttpServletResponse.SC_UNAUTHORIZED)
    }
  }

}