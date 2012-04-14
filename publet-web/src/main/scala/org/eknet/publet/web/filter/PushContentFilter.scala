package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eknet.publet.resource.Content._
import org.eknet.publet.resource.ContentType._
import org.slf4j.LoggerFactory
import org.eknet.publet.resource.{ContentType, Content}
import org.eknet.publet.web.WebContext

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.04.12 00:34
 */
object PushContentFilter extends Filter {
  private val log = LoggerFactory.getLogger(getClass)

  def handle(req: HttpServletRequest, resp: HttpServletResponse) = {
    val publet = WebContext().publet
    val path = WebContext().requestPath
    Option(req.getParameter("page")) match {
      case None =>
      case Some(body) => {
        val target = Option(req.getParameter("type")).getOrElse("markdown")
        log.debug("Write {} file", target)
        publet.push(path, Content(body, ContentType(Symbol(target))))
      }
    }
    false
  }
}
