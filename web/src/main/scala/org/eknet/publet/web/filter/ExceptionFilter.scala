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
import grizzled.slf4j.Logging
import org.eknet.publet.web.{PageWriter, PubletRequestWrapper}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.09.12 17:27
 */
class ExceptionFilter extends Filter with PageWriter with PubletRequestWrapper with Logging {
  def init(filterConfig: FilterConfig) {}

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    try {
      chain.doFilter(request, response)
    }
    catch {
      case e:Throwable => {
        error("Application error", e)
        writeError(e, request, response)
      }
    }
  }

  def destroy() {}
}
