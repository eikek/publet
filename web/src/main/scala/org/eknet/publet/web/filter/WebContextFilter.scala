package org.eknet.publet.web.filter

import javax.servlet.FilterChain
import org.eknet.publet.web.WebContext
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 05:13
 */
class WebContextFilter extends SimpleFilter with PageWriter {

  private val log = LoggerFactory.getLogger(getClass)

  def doFilter(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
    WebContext.setup(req, resp)
    try {
      chain.doFilter(req, resp)
    } catch {
      case e: Throwable => {
        log.error("Error during request: "+ req.getRequestURI, e)
        writeError(e, resp)
      }
    } finally {
      WebContext.clear()
    }
  }

}
