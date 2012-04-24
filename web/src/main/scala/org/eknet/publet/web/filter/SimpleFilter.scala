package org.eknet.publet.web.filter

import javax.servlet._
import http.{HttpServletResponse, HttpServletRequest}


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 05:01
 */
trait SimpleFilter extends Filter {

  def init(filterConfig: FilterConfig) {}

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    doFilter(request.asInstanceOf[HttpServletRequest],
      response.asInstanceOf[HttpServletResponse],
      chain)
  }

  def doFilter(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain)

  def destroy() {}
}
