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
import org.eknet.publet.web.{PubletWebContext, Config, PubletWeb}
import javax.servlet._
import org.eknet.publet.web.webdav.WebdavFilter

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 06:50
 */
class RedirectFilter extends Filter with Logging with HttpFilter {

  private lazy val mount = Config.mainMount

  private lazy val defaultRedirects = Map(
    "/" -> (mount + "/"),
    "/index.html" -> (mount +"/index.html"),
    "/index.htm" -> (mount +"/index.html"),
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

  private def allRedirects = defaultRedirects ++ redirects.map(key => (key.substring(9), PubletWeb.publetSettings(key).get)).toMap

  def doFilter(req: ServletRequest, resp: ServletResponse, chain: FilterChain) {
    val path = PubletWebContext.applicationPath
    if (allRedirects.keySet.contains(path.asString) && !WebdavFilter.isDavRequest) {
      val newUri = allRedirects.get(path.asString).get
      debug("Forward "+ path +" to "+ newUri)
      resp.sendRedirect(PubletWebContext.urlOf(newUri))
    } else {
      chain.doFilter(req, resp)
    }
  }

  def init(filterConfig: FilterConfig) {}

  def destroy() {}
}
