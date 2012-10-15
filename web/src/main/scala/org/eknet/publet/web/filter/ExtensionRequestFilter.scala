package org.eknet.publet.web.filter

import javax.servlet._
import org.eknet.publet.web.PubletRequestWrapper
import org.eknet.publet.web.guice.ExtensionManager

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.06.12 20:48
 */
class ExtensionRequestFilter(extMan: ExtensionManager) extends Filter with PubletRequestWrapper {
  def init(filterConfig: FilterConfig) {}

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
      val req = extMan.executeBeginRequest(request)
      try {
        chain.doFilter(req, response)
      }
      finally {
        extMan.executeEndRequest(request)
      }
  }

  def destroy() {}
}
