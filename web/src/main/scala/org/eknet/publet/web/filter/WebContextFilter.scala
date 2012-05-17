package org.eknet.publet.web.filter

import grizzled.slf4j.Logging
import org.eknet.publet.web.PubletWebContext
import javax.servlet._
import org.apache.shiro.authz.UnauthorizedException

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 05:13
 */
class WebContextFilter extends Filter with HttpFilter with PageWriter with Logging {

  def doFilter(req: ServletRequest, resp: ServletResponse, chain: FilterChain) {
    PubletWebContext.setup(req, resp)
    try {
      chain.doFilter(req, resp)
    } catch {
      case e: Throwable => {
        error("Error during request: "+ req.getRequestURI, e)
        writeError(e, resp)
      }
    } finally {
      PubletWebContext.clear()
    }
  }

  def init(filterConfig: FilterConfig) {}

  def destroy() {}
}
