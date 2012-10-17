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

package org.eknet.publet.web.asset

import javax.servlet._
import org.eknet.publet.web.PubletRequestWrapper
import AssetManager._
import org.eknet.publet.vfs.{ContentType, Path}
import org.eknet.publet.web.filter.Filters.ForwardRequest
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.09.12 18:52
 */
class AssetFilter(assetMgr: AssetManager) extends Filter with PubletRequestWrapper with Logging {

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    if (request.applicationUri.startsWith(groupsPath) || request.applicationUri.startsWith(compressedPath)) {
      chain.doFilter(request, response)
    } else {
      val kind = request.applicationPath.name.targetType match {
        case ContentType.javascript => Some(Kind.js)
        case ContentType.css => Some(Kind.css)
        case _ => None
      }
      kind map { k =>
        val path = request.param("path").map(Path(_))
        val groups = request.params.get("group").getOrElse(List()) match {
          case Nil => List(request.applicationPath.name.name)
          case a@_ => a
        }
        assetMgr.getCompressed(groups, path, k)
      } map { p =>
        chain.doFilter(new ForwardRequest(request.getContextPath+p.asString, request), response)
      } getOrElse {
        chain.doFilter(request, response)
      }
    }
  }


  def init(filterConfig: FilterConfig) {}

  def destroy() {}
}
