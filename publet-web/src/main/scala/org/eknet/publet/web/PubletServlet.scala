package org.eknet.publet.web

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 27.03.12 22:42
 */
class PubletServlet extends HttpServlet {

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    resp.getOutputStream.println("<html><body><h1>Success</h1></body></html>")
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    resp.getOutputStream.println("<html><body><h1>Success</h1></body></html>")
  }
}
