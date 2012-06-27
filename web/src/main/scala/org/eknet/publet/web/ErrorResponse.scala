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

import org.eknet.publet.vfs.{ContentType, Content}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.05.12 19:24
 */
case class ErrorResponse(code: Int) extends Content {
  def contentType = ContentType.unknown
  def inputStream = null

  def send(req: HttpServletRequest, resp: HttpServletResponse) {
    resp.sendError(code)
    req.setAttribute(ErrorResponse.reqKey, this)
  }
}
object ErrorResponse {
  private val reqKey = ErrorResponse.toString

  def errorWritten(req: HttpServletRequest): Option[ErrorResponse] = {
    Option(req.getAttribute(reqKey)).map(_.asInstanceOf[ErrorResponse])
  }

  /** HTTP 405 "Method not allowed */
  val methodNotAllowed = ErrorResponse(HttpServletResponse.SC_METHOD_NOT_ALLOWED)

  /** HTTP 404 "Not found" */
  val notFound = ErrorResponse(HttpServletResponse.SC_NOT_FOUND)

  /** HTTP 500 "Internal Server error" */
  val internalError = ErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)

  /** HTTP 401 "Unauthorized" means not authenticated */
  val unauthorized = ErrorResponse(HttpServletResponse.SC_UNAUTHORIZED)

  /** HTTP 403 "Forbidden" means maybe authenticated but not authorized */
  val forbidden = ErrorResponse(HttpServletResponse.SC_FORBIDDEN)
}