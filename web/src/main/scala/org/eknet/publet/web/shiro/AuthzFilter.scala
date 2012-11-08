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

package org.eknet.publet.web.shiro

import javax.servlet._
import org.apache.shiro.authz.{UnauthenticatedException, UnauthorizedException}
import http.{HttpServletRequest, HttpServletResponse}
import grizzled.slf4j.Logging
import org.eknet.publet.web.{PageWriter, PubletRequestWrapper}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.09.12 16:36
 */
abstract class AuthzFilter(val redirectToLoginPage: Boolean = true) extends Filter with PubletRequestWrapper with Logging {

  def doFilter(req: ServletRequest, resp: ServletResponse, chain: FilterChain) {
    try {
      if (AuthcFilter.authenticationEnabled(req)) {
        checkAccess(req)
      }
      chain.doFilter(req, resp)

    } catch {
      case uae: UnauthorizedException => onUnauthorized(uae, req, resp)
      case se: ServletException if (se.getCause.isInstanceOf[UnauthorizedException]) => {
        onUnauthorized(se.getCause.asInstanceOf[UnauthorizedException], req, resp)
      }
      case ue: UnauthenticatedException => onUnauthenticated(ue, req, resp)
    }
  }

  /**
   * Called if request is not authorized to access the current resource.
   *
   * @param ex
   * @param req
   * @param res
   */
  def onUnauthorized(ex: UnauthorizedException, req: HttpServletRequest, res: HttpServletResponse)

  /**
   * Called if the request is not authenticated yet and not authorized to access
   * the current resource.
   *
   * @param ex
   * @param req
   * @param res
   */
  def onUnauthenticated(ex: UnauthenticatedException, req: HttpServletRequest, res: HttpServletResponse)

  /**
   * This is called before delegating to the filter chain. This method should do
   * acccess checks and throw either [[org.apache.shiro.authz.UnauthenticatedException]]
   * or [[org.apache.shiro.authz.UnauthorizedException]] that is then routed to
   * the two methods `onUnauthenticated` and `onUnauthorized`.
   *
   * This method is only called if the shiro filter has been executed and
   * shiro is setup properly.
   *
   * @param req
   */
  def checkAccess(req: HttpServletRequest) {}

  def init(filterConfig: FilterConfig) {}
  def destroy() {}
}
