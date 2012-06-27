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

import javax.servlet._
import http.HttpServletResponse
import org.eknet.publet.web.PubletWebContext
import grizzled.slf4j.Logging
import org.eknet.publet.web.util.RenderUtils


/**
 * Writes a 404 error into the response for resources
 * that start with a underscore or one of its parents
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.05.12 20:33
 */
class BlacklistFilter extends Filter with HttpFilter with Logging with PageWriter {
  def init(filterConfig: FilterConfig) {}

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    if (!PubletWebContext.isGitRequest) {
      val path = PubletWebContext.applicationPath
      val underscoreSegment = path.segments.find(_.startsWith("_"))
      if (underscoreSegment.isDefined) {
        info("Blacklist-Filter wiping: "+ path.asString)
        writeError(HttpServletResponse.SC_NOT_FOUND, request, response)
        response.flushBuffer()
      }
    }
    if (!response.isCommitted) {
      chain.doFilter(request, response)
    }
  }

  def destroy() {}
}
