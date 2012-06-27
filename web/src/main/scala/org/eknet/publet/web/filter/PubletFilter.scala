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

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import javax.servlet._
import org.eknet.publet.web.{PubletWeb, PubletWebContext}
import org.eknet.publet.vfs.ContentType

/** Servlet that processes the resource using the engine
 * as specified with `a=` http request parameter or the
 * default engine.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:03
 *
 */
class PubletFilter extends Filter with PageWriter with HttpFilter {


  def init(filterConfig: FilterConfig) {}

  def destroy() {}

  def doFilter(req: ServletRequest, resp: ServletResponse, chain: FilterChain) {
    PubletWebContext.param("a") match {
      case Some(engine) => processEngine(req, resp, engine)
      case _ => processDefault(req, resp)
    }
  }

  def processEngine(req: HttpServletRequest, resp: HttpServletResponse, engine: String) {
    val engineId = Symbol(engine)
    val path = PubletWebContext.applicationPath

    val publet = PubletWeb.publet
    val targetType = path.name.targetType
    val someEngine = Some(publet.engineManager.getEngine(engineId).getOrElse(sys.error("No engine '"+engineId+"' available")))
    val html = publet.process(path, targetType, someEngine)
    writePage(html, req, resp)
  }

  def processDefault(req: HttpServletRequest, resp: HttpServletResponse) {
    val publet = PubletWeb.publet
    val path = PubletWebContext.applicationPath

    val tt = if (path.name.targetType == ContentType.unknown) ContentType.html else path.name.targetType
    val html = publet.process(path, tt)
    writePage(html, req, resp)
  }
}
