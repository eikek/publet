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
import org.eknet.publet.vfs.{ContentResource, ContentType}
import org.eknet.publet.web.{PubletWebContext, PubletWeb}

/**
 * Filter that returns the resource as is if it is found. No processing necessary.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.05.12 14:53
 */
class SourceFilter extends Filter with HttpFilter with PageWriter {
  def init(filterConfig: FilterConfig) {}

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val path = PubletWebContext.applicationPath
    PubletWeb.publet.rootContainer.lookup(path)
      .collect({case c:ContentResource=>c}) match {
        case Some(c) => {
          response.setContentType(c.contentType.mimeString)
          c.copyTo(response.getOutputStream)
        }
        case _=> {
          if (!PubletWebContext.isGitRequest && path.name.targetType == ContentType.unknown) {
            writeError(HttpServletResponse.SC_NOT_FOUND, response)
          } else {
            chain.doFilter(request, response)
          }
      }
    }
  }

  def destroy() {}
}
