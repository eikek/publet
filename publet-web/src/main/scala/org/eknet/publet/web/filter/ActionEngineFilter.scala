package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eknet.publet.resource.ContentType
import org.eknet.publet.web.WebContext

/** Filter that processes the resource using the engine
 * as specified with `a=` http request parameter.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:03
 *
 */
object ActionEngineFilter extends Filter with PageWriter {

  def handle(req: HttpServletRequest, resp: HttpServletResponse) = {
    WebContext().action match {
      case Some(engine) => process(req, resp, engine); true
      case _ => false
    }
  }

  def process(req: HttpServletRequest, resp: HttpServletResponse, engine: String) {
    val engineId = Symbol(engine)
    val path = WebContext().requestPath
    val publet = WebContext().publet
    val targetType = path.targetType.getOrElse(ContentType.unknown)
    val html = publet.process(path, targetType, publet.getEngine(engineId).get)
    html.fold(writeError(_, resp), writePage(_, resp))
  }
}
