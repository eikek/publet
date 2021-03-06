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

package org.eknet.publet.web.util

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eknet.publet.web._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 20:35
 */
object PubletWebContext extends RequestAttr with RequestParams with RequestUrl {

  private case class Cycle(req: HttpServletRequest, res: HttpServletResponse)
  private val threadCycle = new ThreadLocal[Cycle]

  private[web] def setup(req: HttpServletRequest, res: HttpServletResponse) {
    threadCycle.set(Cycle(req, res))
  }

  private[web] def clear() {
    threadCycle.remove()
  }

  // ~~~

  private def cycle = Option(threadCycle.get()).getOrElse(sys.error("WebContext not initialized"))
  protected[web] def req = cycle.req
  protected def res = cycle.res


  def redirect(uri: String) {
    res.sendRedirect(uri)
  }

  /**
   * Redirects to the login page adding the full url of
   * the current request as parameter with name `redirect`
   */
  def redirectToLoginPage() {
    new ReqUtils(req).redirectToLoginPage(res)
  }

  def getClientInfo = attr(clientInfoKey).get
  private val clientInfoKey = Key("clientInfo", {
    case Request => ClientInfo(req)
  })

  /**
   * Returns the [[org.eknet.publet.web.ErrorResponse]] that has been
   * written into the response (if so).
   * Meant as a place for filters to know whether the request has been
   * led to an error.
   *
   * @return
   */
  def getErrorResponse = ErrorResponse.errorWritten(req)
}
