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
import RequestHandlerFactory._
import org.eknet.publet.web.shiro.{AuthzFilter, Security}
import org.apache.shiro.authz.{UnauthorizedException, UnauthenticatedException}
import com.google.inject.{Inject, Singleton}
import org.eknet.publet.web.util.{PubletWeb, PubletWebContext}
import org.eknet.publet.web._
import org.eknet.publet.Publet
import org.apache.shiro.SecurityUtils
import org.eknet.publet.auth.ResourcePermissionService

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.09.12 15:29
 */
@Singleton
class PubletHandlerFactory @Inject() (webext: java.util.Set[WebExtension], config: Config, settings: Settings, publet: Publet, permService: ResourcePermissionService) extends RequestHandlerFactory {

  def getApplicableScore(req: HttpServletRequest) = DEFAULT_MATCH

  def createFilter() = new SuperFilter(Seq(
      Filters.redirect(config, settings),
      Filters.webContext,
      Filters.blacklist,
      Filters.authc,
      Filters.exceptionHandler,
      PubletAuthzFilter,
      Filters.extensionRequest(webext),
      Filters.source(publet),
      Filters.publet(publet)
    ))

  object PubletAuthzFilter extends AuthzFilter(redirectToLoginPage = true) with PageWriter {
    override def checkAccess(req: HttpServletRequest) {
      val method = Method.forName(req.getMethod)
      if (method.write)
        permService.checkWrite(req.applicationPath)
      else
        permService.checkRead(req.applicationPath)
    }

    def onUnauthenticated(ex: UnauthenticatedException, req: HttpServletRequest, res: HttpServletResponse) {
      debug("Unauthenticated user for '"+ req.applicationUri+"'. Redirect to login")
      if (redirectToLoginPage) req.redirectToLoginPage(res) else writeError(ex, req, res)
    }

    def onUnauthorized(ex: UnauthorizedException, req: HttpServletRequest, res: HttpServletResponse) {
      error("Unauthorized: "+ ex.getLocalizedMessage)
      writeError(HttpServletResponse.SC_UNAUTHORIZED, req, res)
    }
  }
}
