package org.eknet.publet.web.filter

import javax.servlet._
import org.eknet.publet.web.{PubletWeb, PubletRequestWrapper, WebExtensionLoader}
import org.fusesource.scalate.util.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.06.12 20:48
 */
class ExtensionRequestFilter extends Filter with PubletRequestWrapper with Logging {
  def init(filterConfig: FilterConfig) {}

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
      val req = PubletWeb.instance[WebExtensionLoader].executeBeginRequest(request)
      try {
        chain.doFilter(req, response)
      }
      finally {
        PubletWeb.instance[WebExtensionLoader].executeEndRequest(request)
      }
  }

  def destroy() {}
}
