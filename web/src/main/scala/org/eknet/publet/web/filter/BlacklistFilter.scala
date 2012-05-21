package org.eknet.publet.web.filter

import javax.servlet._
import http.HttpServletResponse
import org.eknet.publet.web.PubletWebContext
import grizzled.slf4j.Logging


/**
 * Writes a 404 error into the response for resources
 * that start with a underscore or one of its parents
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.05.12 20:33
 */
class BlacklistFilter extends Filter with HttpFilter with Logging {
  def init(filterConfig: FilterConfig) {}

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    if (!PubletWebContext.isGitRequest) {
      val path = PubletWebContext.applicationPath
      val underscoreSegment = path.segments.find(_.startsWith("_"))
      if (underscoreSegment.isDefined) {
        info("Blacklist-Filter wiping: "+ path.asString)
        response.sendError(HttpServletResponse.SC_NOT_FOUND)
        response.flushBuffer()
      }
    }
    if (!response.isCommitted) {
      chain.doFilter(request, response)
    }
  }

  def destroy() {}
}
