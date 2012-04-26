package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import javax.servlet.FilterChain
import org.eknet.publet.web.WebContext._
import org.eknet.publet.vfs.Path
import org.slf4j.LoggerFactory
import org.eknet.publet.web.{Config, WebContext}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 06:50
 */
class RedirectFilter extends SimpleFilter {
  private val log = LoggerFactory.getLogger(getClass)
  private lazy val mount = WebContext(mainMount).get
  private val forwards = Set("/",
    "/robots.txt",
    "/index.html",
    "/index.htm",
    "/favicon.ico").map(Path(_))

  // TODO add regex style. redirect /.allIncludes/*
  // TODO add additional redirects from settings

  def doFilter(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
    val path = WebContext().requestPath
    val contextPath = WebContext().getContextPath.getOrElse("")
    if (forwards.contains(path)) {
      val newUri = contextPath + "/"+ mount + path.asString
      log.debug("Forward "+ path +" to "+ newUri)
      resp.sendRedirect(newUri)
    }
    else
      chain.doFilter(req, resp)
  }

  private def printthings(req: HttpServletRequest) {
    log.info("servletPath: "+ req.getServletPath)
    log.info("contextPath: "+ req.getContextPath)
    log.info("pathInfo: "+ req.getPathInfo)
    log.info("pathTranslated: "+ req.getPathTranslated)
    log.info("remoteAddr: "+ req.getRemoteAddr)
    log.info("remoteHost: "+ req.getRemoteHost)
    log.info("remotePort: "+ req.getRemotePort)
  }
}
