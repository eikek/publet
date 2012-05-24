package org.eknet.publet.web

import org.eknet.publet.vfs.{ContentType, Content}
import javax.servlet.http.HttpServletResponse


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.05.12 19:24
 */
case class ErrorResponse(code: Int) extends Content {
  def contentType = ContentType.unknown
  def inputStream = null

  def send(resp: HttpServletResponse) {
    resp.sendError(code)
  }
}
object ErrorResponse {

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