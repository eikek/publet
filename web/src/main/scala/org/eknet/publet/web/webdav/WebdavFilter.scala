package org.eknet.publet.web.webdav

import javax.servlet._
import http.HttpServletRequest
import org.eknet.publet.web.filter.HttpFilter
import com.bradmcevoy.http.{Response, Request, AuthenticationService, HttpManager}
import org.eknet.publet.web.{PartitionMounter, PubletWeb}
import org.eknet.publet.vfs.Path

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 21:02
 */
class WebdavFilter extends Filter with HttpFilter {

  def init(filterConfig: FilterConfig) {}
  def destroy() {}

  lazy val httpManager = {
    import collection.JavaConversions._
    new HttpManager(new WebdavResourceFactory, new AuthenticationService(List(new WebdavAuthHandler)))
  }

  def doFilter(req: ServletRequest, resp: ServletResponse, chain: FilterChain) {
    if (WebdavFilter.isDavRequest(req)) {
      import com.bradmcevoy
      val request: Request = new bradmcevoy.http.ServletRequest(req, PubletWeb.servletContext)
      val response: Response = new bradmcevoy.http.ServletResponse(resp)
      httpManager.process(request, response)
    } else {
      chain.doFilter(req, resp)
    }
  }
}

object WebdavFilter {

  /**
   * Returns whether the request is pointing to a resource that
   * is mounted as webdav resource.
   *
   * @param req
   * @return
   */
  def isDavRequest(req: HttpServletRequest): Boolean = {
    val reqPath = Path(req.getRequestURI)
    var dav = false
    PartitionMounter.applyMounts("webdav", (dir, mount) => {
      if (reqPath.prefixedBy(mount)) {
        dav = true
      }
    })
    dav
  }
}
