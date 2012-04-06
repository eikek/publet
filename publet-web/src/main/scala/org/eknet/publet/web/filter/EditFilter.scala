package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eknet.publet.resource.ContentType

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:03
 *
 */
object EditFilter extends Filter with PageWriter {

  def handle(req: HttpServletRequest, resp: HttpServletResponse) = {
    action(req) match {
      case Some("edit") => edit(req, resp); true
      case _ => false
    }
  }

  def edit(req: HttpServletRequest, resp: HttpServletResponse) {
    if (path(req).targetType.get.mime._1 == "text") {
      val html = publet(req).process(path(req), ContentType.markdown, publet(req).getEngine('edit).get)
      html.fold(writeError(_, path(req), resp), writePage(_, path(req), req, resp))
    } else {
      createNew(path(req), req, resp)
    }
  }
}
