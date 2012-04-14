package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eknet.publet.resource.ContentType
import org.eknet.publet.web.WebContext

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:02
 *
 */
object PublishFilter extends Filter with PageWriter {

  def handle(req: HttpServletRequest, resp: HttpServletResponse) = {
    val publet = WebContext().publet
    val path = WebContext().requestPath
    val html = publet.process(path, path.targetType.getOrElse(ContentType.html))
    html.fold(writeError(_, path, resp), writePage(_, path, req, resp))
    true
  }

}
