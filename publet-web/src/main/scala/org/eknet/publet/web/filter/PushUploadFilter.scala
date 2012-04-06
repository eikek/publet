package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eknet.publet.resource.Content._
import org.slf4j.LoggerFactory
import org.eknet.publet.resource.Content

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:08
 *
 */
object PushUploadFilter extends Filter with FilterContext {
  private val log = LoggerFactory.getLogger(getClass)

  def handle(req: HttpServletRequest, resp: HttpServletResponse) = {
    uploads(req).foreach(fi => {
      log.debug("Create {} file", path(req).targetType.get)
      publet(req).push(path(req), Content(fi.getInputStream, path(req).targetType.get))
    })
    false
  }
}
