package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eknet.publet.resource.ContentType

/** Filter that processes the resource using the engine
 * as specified with `a=` http request parameter.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:03
 *
 */
object ActionEngineFilter extends Filter with PageWriter {

  def handle(req: HttpServletRequest, resp: HttpServletResponse) = {
    action(req) match {
      case Some(engine) => process(req, resp, engine); true
      case _ => false
    }
  }

  def process(req: HttpServletRequest, resp: HttpServletResponse, engine: String) {
    val engineId = Symbol(engine)
    if (path(req).targetType.get.mime._1 == "text") {
      val html = publet(req).process(path(req), path(req).targetType.get, publet(req).getEngine(engineId).get)
      html.fold(writeError(_, path(req), resp), writePage(_, path(req), req, resp))
    } else {
      createNew(path(req), req, resp)
    }
  }
}
