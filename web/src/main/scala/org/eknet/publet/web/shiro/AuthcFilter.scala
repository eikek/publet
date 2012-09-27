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

import org.apache.shiro.web.servlet.ShiroFilter
import javax.servlet.{ServletResponse, ServletRequest}
import javax.servlet.http.HttpServletRequest

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.09.12 17:30
 */
class AuthcFilter extends ShiroFilter {
  override final def isEnabled(request: ServletRequest, response: ServletResponse) = {
    AuthcFilter.isEnabled(request) getOrElse {
      val enabled = isEnabled(request.asInstanceOf[HttpServletRequest])
      AuthcFilter.setEnabled(request, enabled)
      enabled
    }
  }

  def isEnabled(req: HttpServletRequest): Boolean = true
}

object AuthcFilter {
  private val filterEnabledKey: String = "org.eknet.publet.publetShiroFilterEnabled"

  private def isEnabled(req: ServletRequest):Option[Boolean] =
    Option(req.getAttribute(filterEnabledKey)).map(_.asInstanceOf[Boolean])
  private def setEnabled(req: ServletRequest, enabled: Boolean) {
    req.setAttribute(filterEnabledKey, enabled)
  }

  def authenticationEnabled(req: ServletRequest) = isEnabled(req).exists(_ == true)
}
