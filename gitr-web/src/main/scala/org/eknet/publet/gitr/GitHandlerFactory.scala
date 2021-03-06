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

package org.eknet.publet.gitr

import com.google.common.eventbus.EventBus
import com.google.inject.{Inject, Singleton}
import grizzled.slf4j.Logging
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authz.{UnauthorizedException, UnauthenticatedException}
import org.eknet.gitr.GitrMan
import org.eknet.publet.gitr.auth.{GitPermissionBuilder, GitAction, RepositoryTag}
import org.eknet.publet.web.{Config, PubletRequestWrapper}
import org.eknet.publet.web.filter._
import org.eknet.publet.web.req.RequestHandlerFactory._
import org.eknet.publet.web.req.{SuperFilter, RequestHandlerFactory}
import org.eknet.publet.web.shiro.{AuthzFilter, AuthcFilter}
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter
import org.apache.shiro.web.filter.mgt.{PathMatchingFilterChainResolver, FilterChainResolver}
import javax.servlet.{ServletResponse, ServletRequest}
import org.eknet.publet.web.util.{Request, Key}
import org.eknet.publet.vfs.Path

/**
 * Creates a filter chain to server git requests.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.09.12 15:18
 */
@Singleton
class GitHandlerFactory @Inject() (gitr: GitrMan, bus: EventBus, config: Config) extends RequestHandlerFactory with PubletRequestWrapper with Logging {

  import org.eknet.publet.gitr.GitRequestUtils._

  def getApplicableScore(req: HttpServletRequest) =
    if (req.isGitRequest) EXACT_MATCH else NO_MATCH

  def createFilter() = new SuperFilter(Seq(
      Filters.webContext,
      GitShiroFilter,
      Filters.exceptionHandler,
      GitAuthzFilter,
      new GitHttpFilter(gitr, bus)
    ))

  object GitShiroFilter extends AuthcFilter {

    private val resolver = {
      val resolver = new PathMatchingFilterChainResolver()
      resolver.getFilterChainManager.addFilter("authcBasic", new BasicHttpAuthenticationFilter)
      val gitPath = Path(GitRequestUtils.gitMount(config)) / "**"
      resolver.getFilterChainManager.createChain(gitPath.toAbsolute.asString, "authcBasic")
      setFilterChainResolver(resolver)
      resolver
    }

    override def init() {
      super.init()
      setFilterChainResolver(resolver)
    }

    override def isEnabled(request: HttpServletRequest) = {
      val action = request.getGitAction
      val tag = request.getRepositoryModel
        .map(_.tag)
        .getOrElse(RepositoryTag.open)

      val anon = tag == RepositoryTag.open && action.exists(_ == GitAction.pull)
      !anon
    }
  }

  object GitAuthzFilter extends AuthzFilter with GitPermissionBuilder {

    override def checkAccess(req: HttpServletRequest) {
      val gitAction = req.getGitAction.getOrElse(GitAction.pull)
      val repoModel = req.getRepositoryModel
      repoModel.foreach { repo =>
        val perm = git action(gitAction) on repo.name
        SecurityUtils.getSubject.checkPermission(perm.perm)
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