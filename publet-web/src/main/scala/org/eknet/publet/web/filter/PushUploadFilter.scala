package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.slf4j.LoggerFactory
import org.eknet.publet.resource.Content
import org.eknet.publet.web.WebContext

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:08
 *
 */
object PushUploadFilter extends Filter {
  private val log = LoggerFactory.getLogger(getClass)

  def handle(req: HttpServletRequest, resp: HttpServletResponse) = {
    val ctx = WebContext()
    ctx.uploads.foreach(fi => {
      log.debug("Create {} file", ctx.requestPath.targetType.get)
      ctx.publet.push(ctx.requestPath, Content(fi.getInputStream, ctx.requestPath.targetType.get))
    })
    false
  }
}
