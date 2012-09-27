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

import grizzled.slf4j.Logging
import org.eknet.publet.web.{PubletRequestWrapper, Config, PubletWeb}
import javax.servlet._
import http.{HttpServletRequest, HttpServletRequestWrapper}

/**
 * The first filter on the chain. Detects whether the current
 * request should be redirected or forwarded. On either no more
 * work is necessary.
 *
 * Doing a forward will only hand a modified request to the next
 * filter in the chain. Thus, this filter needs to be before the
 * one that sets up the thread; usually it's good to put this filter
 * as the first one in the chain.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 06:50
 */
class RedirectFilter extends Filter with Logging with PubletRequestWrapper {

  private lazy val mount = Config.mainMount

  private lazy val defaultRedirects = Map(
    "/" -> (mount + "/"),
    "/index.html" -> (mount +"/index.html"),
    "/index.htm" -> (mount +"/index.html")
  )

  private val defaultForwards = Map(
    "/robots.txt" -> (mount +"/robots.txt"),
    "/favicon.ico" -> (mount +"/favicon.ico"),
    "/favicon.png" -> (mount +"/favicon.png")
  )

  /**
   * Returns a list of property keys from `settings.properties`
   * for those that start with `redirect.`
   *
   * @return
   */
  private def redirects = PubletWeb.publetSettings.keySet.filter(_.startsWith("redirect."))

  /**
   * Returns a list of property keys from `settings.properties`
   * for those that start with `forward.`
   * @return
   */
  private def forwards = PubletWeb.publetSettings.keySet.filter(_.startsWith("forward."))

  private def allRedirects = defaultRedirects ++ redirects.map(key => (key.substring(9), PubletWeb.publetSettings(key).get)).toMap
  private def allForwards = defaultForwards ++ forwards.map(key => (key.substring(8), PubletWeb.publetSettings(key).get)).toMap

  def doFilter(req: ServletRequest, resp: ServletResponse, chain: FilterChain) {
    val path = req.applicationPath
    if (allRedirects.keySet.contains(path.asString)) {
      val newUri = allRedirects.get(path.asString).get
      debug("Redirect "+ path +" to "+ newUri)
      resp.sendRedirect(req.urlOf(newUri))
    } else if (allForwards.keySet.contains(path.asString)) {
      val newUri = allForwards.get(path.asString).get
      debug("Forward "+ path +" to "+ newUri)
      val forwardingReq = new ForwardRequest(newUri, req)
      chain.doFilter(forwardingReq, resp)
    } else {
      chain.doFilter(req, resp)
    }
  }

  def init(filterConfig: FilterConfig) {}

  def destroy() {}

  private class ForwardRequest(uri: String, req: HttpServletRequest) extends HttpServletRequestWrapper(req) {
    import collection.JavaConversions._

    req.getAttributeNames.toList.withFilter(!_.contains("eclipse.jetty")).foreach(key => req.removeAttribute(key))

    override val getRequestURI = if (!uri.startsWith("/")) "/"+uri else uri
  }
}

