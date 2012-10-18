package org.eknet.publet.web.filter

import javax.servlet._
import org.eknet.publet.web.{WebExtension, PubletRequestWrapper}
import javax.servlet.http.HttpServletRequest
import collection.JavaConversions._
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.06.12 20:48
 */
class ExtensionRequestFilter(webext: java.util.Set[WebExtension]) extends Filter with PubletRequestWrapper with Logging {
  def init(filterConfig: FilterConfig) {}

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
      val req = executeBeginRequest(request)
      try {
        chain.doFilter(req, response)
      }
      finally {
        executeEndRequest(request)
      }
  }

  def destroy() {}

  private def safely[A](errorMsg: => String)(body: () => A): Option[A] = {
    try {
      Some(body())
    } catch {
      case e: Exception => error(errorMsg, e); None
    }
  }

  def executeBeginRequest(req: HttpServletRequest): HttpServletRequest = {
    webext.foldLeft(req)((r1, ext) => {
      safely("Exception invoking onBeginRequest of extension '"+ ext +"'!") { () =>
        ext.onBeginRequest(r1)
      } getOrElse(r1)
    })
  }

  def executeEndRequest(req:HttpServletRequest) {
    webext.foreach(ext => safely("Exception invoking onEndRequest of extension '"+ ext +"'!") { () =>
      ext.onEndRequest(req)
    })
  }
}
