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

package org.eknet.publet.web

import javax.servlet.http.HttpServletRequest
import com.google.inject.Module

/**
 * @since 26.04.12 20:31
 */
trait WebExtension {

  /**
   * This method is invoked on the begin of each request. It allows
   * to wrap the given request. For example to forward to another
   * resource.
   *
   * The request at this point is already authenticated and authorized.
   * See [[org.eknet.publet.web.req.PubletHandlerFactory]] for details.
   *
   * Requests to the git repository and to a webdav resources are
   * not routed here.
   *
   * If you just want to be informed, you could also subscribe to
   * the [[org.eknet.publet.web.req.RequestStartedEvent]] or
   * [[org.eknet.publet.web.req.RequestEndEvent]]
   */
  def onBeginRequest(req:HttpServletRequest): HttpServletRequest

  /**
   * This method is invoked on the end of each request.
   *
   */
  def onEndRequest(req: HttpServletRequest)

}