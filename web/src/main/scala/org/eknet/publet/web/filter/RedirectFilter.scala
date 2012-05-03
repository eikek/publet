package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import javax.servlet.FilterChain
import org.eknet.publet.web.WebContext._
import org.eknet.publet.vfs.Path
import org.slf4j.LoggerFactory
import grizzled.slf4j.Logging
import org.eknet.publet.Includes
import org.eknet.publet.web.{Settings, Config, WebContext}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 06:50
 */
class RedirectFilter extends SimpleFilter with Logging {

  private lazy val mount = WebContext(mainMount).get

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
  private lazy val redirects = Settings.keySet.filter(_.startsWith("redirect."))

  private lazy val allRedirects = defaultRedirects ++ redirects.map(key => (key.substring(9), Settings(key).get)).toMap

  def doFilter(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
    val path = WebContext().requestPath
    val contextPath = WebContext.getContextPath().getOrElse("")
    if (allRedirects.keySet.contains(path.asString)) {
      val newUri = contextPath + allRedirects.get(path.asString).get
      debug("Forward "+ path +" to "+ newUri)
      resp.sendRedirect(newUri)
    } else {
      chain.doFilter(req, resp)
    }
  }

}
