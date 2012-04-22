package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.slf4j.LoggerFactory
import org.eknet.publet.resource.Content
import org.eknet.publet.web.WebContext
import javax.servlet.{FilterChain, Filter}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:08
 *
 */
class PushUploadFilter extends Filter with SimpleFilter {
  private val log = LoggerFactory.getLogger(getClass)


  def doFilter(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
    val ctx = WebContext()
    ctx.uploads.foreach(fi => {
      log.debug("Create {} file", ctx.requestPath.targetType.get)
      ctx.publet.push(ctx.requestPath, Content(fi.getInputStream, ctx.requestPath.targetType.get))
    })
    chain.doFilter(req, resp)
  }

}
