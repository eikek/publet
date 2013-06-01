package org.eknet.publet.webapp.servlet

import spray.http.HttpRequest

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.05.13 16:28
 */
case class RequestSettings(request: HttpRequest = null,
                           bindAddress: String = "",
                           bindPort: Int = -1,
                           servletPath: String = "",
                           remoteUser: Option[String] = None) {

  def forRequest(req: HttpRequest) = copy(request = req)
}
