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
import org.eknet.publet.auth.GitAction
import org.eknet.publet.web.{ErrorResponse, RepositoryNameResolver, PubletWeb, PubletWebContext}
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.webdav.WebdavFilter

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

      // Webdav filter handles that itself. Initiates BASIC
      // auth challenges on UnauthenticatedException, for example
      if (!WebdavFilter.isDavRequest) {
        AuthzFilter.checkAccessToCurrentResource()
      }

      chain.doFilter(req, resp)

    } catch {
      case uae: UnauthorizedException => {
        error("Unauthorized: "+ uae.getLocalizedMessage)
        writeError(HttpServletResponse.SC_UNAUTHORIZED, req, resp)
      }
      case se: ServletException if (se.getCause.isInstanceOf[UnauthorizedException]) => {
        error("Unauthorized: "+ se.getCause.getLocalizedMessage)
        writeError(HttpServletResponse.SC_UNAUTHORIZED, req, resp)
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

object AuthzFilter {

  /**
   * Checks if the current request is allowed to access the resource.
   * Throws an [[org.apache.shiro.authz.UnauthorizedException]] if it
   * fails.
   *
   */
  def checkAccessToCurrentResource() {
    if (!PubletShiroFilter.anonRequestAllowed) {
      val repoModel = PubletWebContext.getRepositoryModel
      val gitAction = PubletWebContext.getGitAction.getOrElse(GitAction.pull)

      repoModel.foreach { repo =>
        Security.checkGitAction(gitAction, repo)
      }
      PubletWeb.authManager.getResourceConstraints(PubletWebContext.applicationUri)
        .filterNot(_.perm.isAnon)
        .foreach(rc => Security.checkPerm(rc.perm.permString))
    }
  }

  /**
   * Returns whether the current request can access the resource at
   * the specified uri.
   *
   * The request is allowed, if the resource is explicitely marked
   * with an `anon` permission. If it is marked with another permission,
   * it checks the permission against the principal of the current request.
   *
   * If no permission is specified, it is checked whether the resource
   * belongs to a git repository. If it is an open git repository, access
   * is granted. Otherwise `pull` permission is checked.
   *
   * If the resource it not marked with an explicit permission and neither
   * belongs to a git repository, it is considered an open resource and
   * any request may access it.
   *
   * @param applicationUri
   * @return
   */
  def hasAccessToResource(applicationUri: String): Boolean = {
    lazy val repoModel = RepositoryNameResolver
      .getRepositoryName(Path(applicationUri), isGitRequest = false)
      .map(name => PubletWeb.authManager.getRepository(name.name))

    lazy val hasPull = repoModel map { repoModel =>
      Security.hasGitAction(GitAction.pull, repoModel)
    }

    PubletWeb.authManager.getResourceConstraints(applicationUri).map(rc => {
      if (rc.perm.isAnon) true
      else Security.hasPerm(rc.perm.permString)
    }) getOrElse {
      hasPull getOrElse (true)
    }
  }
}