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

package org.eknet.publet.web

import guice.PubletShiroModule
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import javax.servlet.{ServletResponse, ServletRequest}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 23:08
 */
trait PubletRequestWrapper {

  implicit def toHttpReq(request: ServletRequest) = request.asInstanceOf[HttpServletRequest]
  implicit def toHttpRes(res: ServletResponse) = res.asInstanceOf[HttpServletResponse]
  implicit def toRequestUtils(req: ServletRequest): ReqUtils = new ReqUtils(req)

}
class ReqUtils(val req: HttpServletRequest) extends
    RequestAttr with RequestUrl with RequestParams with RepositoryNameResolver {

  /**
   * Redirects to the login page adding the full url of
   * the current request as parameter with name `redirect`
   */
  def redirectToLoginPage(res: HttpServletResponse) {
    val p = params.map(t => t._1 +"="+ t._2.mkString(",")).mkString("&")
    val uri = applicationUri + (if (p.isEmpty) "" else "?"+p)
    val loginPath = PubletWeb.instance[String]("loginPath")
    res.sendRedirect(urlOf(loginPath)+"?redirect="+ urlOf(uri))
  }
}
