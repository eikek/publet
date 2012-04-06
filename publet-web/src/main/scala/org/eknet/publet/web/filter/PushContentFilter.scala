package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eknet.publet.resource.Content._
import org.eknet.publet.resource.ContentType._
import org.slf4j.LoggerFactory
import org.eknet.publet.resource.{ContentType, Content}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.04.12 00:34
 */
object PushContentFilter extends Filter with FilterContext {
  private val log = LoggerFactory.getLogger(getClass)

  def handle(req: HttpServletRequest, resp: HttpServletResponse) = {
    Option(req.getParameter("page")) match {
      case None =>
      case Some(body) => {
        val target = Option(req.getParameter("type")).getOrElse("markdown")
        log.debug("Write {} file", target)
        publet(req).push(path(req), Content(body, ContentType(Symbol(target))))
      }
    }
    false
  }
}
