package org.eknet.publet.web.filter

import grizzled.slf4j.Logging
import org.eknet.publet.web.{PubletWebContext, Config, PubletWeb}
import javax.servlet._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 06:50
 */
class RedirectFilter extends Filter with Logging with HttpFilter {

  private lazy val mount = Config.mainMount

  private lazy val defaultRedirects = Map(
    "/" -> (mount + "/"),
    "/index.html" -> (mount +"/index.html"),
    "/index.htm" -> (mount +"/index.html"),
    "/robots.txt" -> (mount +"/robots.txt"),
    "/favicon.ico" -> (mount +"/favicon.ico"),
    "/favicon.png" -> (mount +"/favicon.png")
  )

  /**
   * Returns a list of property keys from `settings.properties`
   * for those that start with `redirect.`
   *
   * @return
   */
  private lazy val redirects = PubletWeb.publetSettings.keySet.filter(_.startsWith("redirect."))

  private lazy val allRedirects = defaultRedirects ++ redirects.map(key => (key.substring(9), PubletWeb.publetSettings(key).get)).toMap

  def doFilter(req: ServletRequest, resp: ServletResponse, chain: FilterChain) {
    val path = PubletWebContext.applicationPath
    if (allRedirects.keySet.contains(path.asString)) {
      val newUri = allRedirects.get(path.asString).get
      debug("Forward "+ path +" to "+ newUri)
      resp.sendRedirect(newUri)
    } else {
      chain.doFilter(req, resp)
    }
  }

  def init(filterConfig: FilterConfig) {}

  def destroy() {}
}
