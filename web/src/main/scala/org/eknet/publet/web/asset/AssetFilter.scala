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
import org.eknet.publet.web.filter.Filters
import AssetManager._
import org.eknet.publet.vfs.{ContentType, Path}
import org.eknet.publet.web.filter.Filters.ForwardRequest

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.09.12 18:52
 */
class AssetFilter extends Filter with PubletRequestWrapper {

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    if (request.applicationUri.startsWith(assetPath)) {
      if (request.applicationUri.startsWith(partsPath) || request.applicationUri.startsWith(compressedPath)) {
        Filters.source.doFilter(request, response, chain)
      } else {
        val path = request.param("path").map(Path(_)).getOrElse(Path.root)
        val contentType = request.applicationPath.name.targetType
        if (contentType == ContentType.javascript) {
          val target = AssetExtension.assetManager.getCompressed(request.applicationPath.name.name, path, Kind.js)
          Filters.source.doFilter(new ForwardRequest(request.getContextPath + target.asString, request), response, chain)
        }
        else if (contentType == ContentType.css) {
          val target = AssetExtension.assetManager.getCompressed(request.applicationPath.name.name, path, Kind.css)
          Filters.source.doFilter(new ForwardRequest(request.getContextPath + target.asString, request), response, chain)
        }
        else {
          chain.doFilter(request, response)
        }
      }
    } else {
      chain.doFilter(request, response)
    }
  }

  def init(filterConfig: FilterConfig) {}

  def destroy() {}
}
