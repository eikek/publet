package org.eknet.publet.web.filter

import org.eknet.publet.vfs.Path
import javax.servlet.http.HttpServletResponse

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 16:10
 */
trait NotFoundHandler {

  def resourceNotFound(path: Path, resp: HttpServletResponse)

}
