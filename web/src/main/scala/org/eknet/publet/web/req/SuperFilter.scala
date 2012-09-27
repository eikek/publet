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

package org.eknet.publet.web.req

import javax.servlet._

/**
 * A filter which is itself a filter chain.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.09.12 15:09
 */
class SuperFilter(val filters: Seq[Filter]) extends Filter {

  class Chain(filters: Seq[Filter], chain:FilterChain) extends FilterChain {
    def doFilter(request: ServletRequest, response: ServletResponse) {
      filters match {
        case f :: fs => f.doFilter(request, response, new Chain(fs, chain))
        case Nil => chain.doFilter(request, response)
      }
    }
  }

  def init(filterConfig: FilterConfig) {
    filters.foreach(_.init(filterConfig))
  }

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    new Chain(filters, chain).doFilter(request, response)
  }

  def destroy() {
    filters.foreach(_.destroy())
  }

}
