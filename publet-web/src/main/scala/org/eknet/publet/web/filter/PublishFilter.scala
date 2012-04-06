package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eknet.publet.resource.ContentType

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:02
 *
 */
object PublishFilter extends Filter with PageWriter {

  def handle(req: HttpServletRequest, resp: HttpServletResponse) = {
    val html = publet(req).process(path(req), path(req).targetType.getOrElse(ContentType.html))
    html.fold(writeError(_, path(req), resp), writePage(_, path(req), req, resp))
    true
  }

}
