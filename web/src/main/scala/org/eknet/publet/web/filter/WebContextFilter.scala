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
import javax.servlet._
import org.eknet.publet.web.PubletRequestWrapper
import org.eknet.publet.web.util.PubletWebContext

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 05:13
 */
class WebContextFilter extends Filter with PubletRequestWrapper with Logging {

  def doFilter(req: ServletRequest, resp: ServletResponse, chain: FilterChain) {
    PubletWebContext.setup(req, resp)
    try {
      chain.doFilter(req, resp)
    } finally {
      PubletWebContext.clear()
    }
  }

  def init(filterConfig: FilterConfig) {}

  def destroy() {}
}
