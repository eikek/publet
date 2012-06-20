package org.eknet.publet.web.filter

import javax.servlet._
import org.eknet.publet.web.WebExtensionLoader
import org.fusesource.scalate.util.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.06.12 20:48
 */
class ExtensionRequestFilter extends Filter with HttpFilter with Logging {
  def init(filterConfig: FilterConfig) {}

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
      WebExtensionLoader.executeBeginRequest()
      try {
        chain.doFilter(request, response)
      }
      finally {
        WebExtensionLoader.executeEndRequest()
      }
  }

  def destroy() {}
}
