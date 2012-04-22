package org.eknet.publet.web.filter

import org.eknet.publet.resource.ContentType
import org.eknet.publet.web.WebContext
import javax.servlet.http.{HttpServlet, HttpServletResponse, HttpServletRequest}

/** Servlet that processes the resource using the engine
 * as specified with `a=` http request parameter or the
 * default engine.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:03
 *
 */
class PublishServlet extends HttpServlet with PageWriter {

  override def service(req: HttpServletRequest, resp: HttpServletResponse) = {
    WebContext().action match {
      case Some(engine) => processEngine(req, resp, engine); true
      case _ => processDefault(req, resp)
    }
  }

  def processEngine(req: HttpServletRequest, resp: HttpServletResponse, engine: String) {
    val engineId = Symbol(engine)
    val path = WebContext().requestPath
    val publet = WebContext().publet
    val targetType = path.targetType.getOrElse(ContentType.unknown)
    val html = publet.process(path, targetType, publet.getEngine(engineId).get)
    html.fold(writeError(_, resp), writePage(_, resp))
  }

  def processDefault(req: HttpServletRequest, resp: HttpServletResponse) {
    val publet = WebContext().publet
    val path = WebContext().requestPath
    val html = publet.process(path, path.targetType.getOrElse(ContentType.html))
    html.fold(writeError(_, resp), writePage(_, resp))
  }
}
