package org.eknet.publet.web

import filter.{PageWriter, SuperFilter}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import org.slf4j.LoggerFactory
import java.io.File

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 27.03.12 22:42
 */
class PubletServlet extends HttpServlet with PageWriter {

  private val log = LoggerFactory.getLogger(getClass)
  private val filter = SuperFilter()


  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    WebContext.setup(req)
    try {
      filter.handle(req, resp)
    } catch {
      case t:Throwable => {
        log.error("Error for "+ req.getRequestURI, t)
        writeError(t, resp)
      }
    }
    WebContext.clear()
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    WebContext.setup(req)
    try {
      filter.handle(req, resp)
    } catch {
      case t:Throwable => {
        log.error("Error for "+ req.getRequestURI, t)
        writeError(t, resp)
      }
    }
    WebContext.clear()
  }
}
