package org.eknet.publet.webdav

import javax.servlet._
import org.eknet.publet.web._
import com.bradmcevoy.http.{Response, Request, MiltonServlet, HttpManager}
import ref.WeakReference

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 21:02
 */
class WebdavFilter extends Filter with PubletRequestWrapper {

  private var servletContext: WeakReference[ServletContext] = null
  private var httpManager: HttpManager = null

  def init(filterConfig: FilterConfig) {
    this.servletContext = new WeakReference(filterConfig.getServletContext)
    this.httpManager = new HttpManager(new WebdavResourceFactory)
  }

  def destroy() {
    if (httpManager != null) {
      httpManager.shutdown()
    }
  }

  def doFilter(req: ServletRequest, resp: ServletResponse, chain: FilterChain) {
    import com.bradmcevoy.http
    try {
      MiltonServlet.setThreadlocals(req, resp)
      val request: Request = new http.ServletRequest(req, servletContext())
      val response: Response = new http.ServletResponse(resp)
      httpManager.process(request, response)
    } finally {
//        http.ServletRequest.clearThreadLocals() <- this is package-private. that's bad ,because that means uncleared thread-locals.
      MiltonServlet.clearThreadlocals()
      resp.getOutputStream.flush()
      resp.flushBuffer()
    }
  }
}
