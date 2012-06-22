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

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import util.{Request, Key, AttributeMap}
import java.util.Locale


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 20:35
 */
object PubletWebContext extends RequestParams with RequestUrl with RepositoryNameResolver {

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
  protected def req = cycle.req
  protected def res = cycle.res


  def sessionMap = AttributeMap(req.getSession)
  def requestMap = AttributeMap(req)
  def contextMap = PubletWeb.contextMap

  def attr[T: Manifest](key: Key[T]) = {
    requestMap.get(key).orElse {
      sessionMap.get(key).orElse {
        contextMap.get(key)
      }
    }
  }

  def redirect(uri: String) {
    res.sendRedirect(uri)
  }

  /**
   * Redirects to the login page adding the full url of
   * the current request as parameter with name `redirect`
   */
  def redirectToLoginPage() {
    val p = params.map(t => t._1 +"="+ t._2.mkString(",")).mkString("&")
    val uri = applicationUri + (if (p.isEmpty) "" else "?"+p)
    redirect(urlOf(PubletWeb.getLoginPath)+"?redirect="+ urlOf(uri))
  }

  /**
   * Returns the http request method.
   * @return
   */
  def getMethod: Method.Value = Method.withName(req.getMethod.toUpperCase(Locale.ROOT))

  def getLocale = req.getLocale

  def getClientInfo = attr(clientInfoKey).get
  private val clientInfoKey = Key("clientInfo", {
    case Request => ClientInfo(req)
  })
}
