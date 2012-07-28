package org.eknet.publet.web.webdav

import javax.servlet._
import org.eknet.publet.web.filter.HttpFilter
import org.eknet.publet.web.{Method, PubletWebContext, Config, PubletWeb}
import org.eknet.publet.web.util.Key
import java.util
import com.bradmcevoy.http.{Response, Request, MiltonServlet, HttpManager}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 21:02
 */
class WebdavFilter extends Filter with HttpFilter {

  private var servletContext:ServletContext = null
  private var httpManager: HttpManager = null

  def init(filterConfig: FilterConfig) {
    this.servletContext = filterConfig.getServletContext
    this.httpManager = new HttpManager(new WebdavResourceFactory)
  }

  def destroy() {
    if (httpManager != null) {
      httpManager.shutdown()
    }
  }

  def doFilter(req: ServletRequest, resp: ServletResponse, chain: FilterChain) {
    if (WebdavFilter.isDavRequest) {
      import com.bradmcevoy.http
      try {
        MiltonServlet.setThreadlocals(req, resp)
        val request: Request = new http.ServletRequest(req, servletContext)
        val response: Response = new http.ServletResponse(resp)
        httpManager.process(request, response)
      } finally {
//        http.ServletRequest.clearThreadLocals() <- this is package-private. that's bad ,because that means uncleared thread-locals.
        MiltonServlet.clearThreadlocals()
        resp.getOutputStream.flush()
        resp.flushBuffer()
      }
    } else {
      chain.doFilter(req, resp)
    }
  }
}

object WebdavFilter {

  private val webdavFilterKey = Key(getClass.getName, {
    case org.eknet.publet.web.util.Request => {
      //for windows clients: they probe the server with an OPTIONS request to the root
      //thus, we should let this go to milton.
      isDavRequest(PubletWebContext.applicationUri) ||
        (PubletWebContext.applicationPath.parent.isRoot && PubletWebContext.getMethod == Method.options)
    }
  })

  /**
   * Returns whether the current request is handled by the webdav filter
   *
   * @return
   */
  def isDavRequest:Boolean = PubletWebContext.attr(webdavFilterKey).get


  /**
   * Returns whether the request is pointing to a resource that
   * is mounted as webdav resource.
   *
   * @param path the request uri path
   * @return
   */
  def isDavRequest(path: String): Boolean = {
    if (!Config("webdav.enabled").map(_.toBoolean).getOrElse(true)) {
      false
    } else {
      getWebdavFilterUrls.exists(url => path.startsWith(url))
    }
  }

  /**
   * Returns all configured url prefixes that are handled by the
   * webdav filter. The list is cached in the current session and
   * re-fetched if missing.
   *
   * @return
   */
  def getWebdavFilterUrls = PubletWebContext.attr(Key("webdavFilterUrls", {
    case org.eknet.publet.web.util.Session => {
      def recurseFind(num: Int): List[String] = {
        val key = "webdav.filter."+num
        PubletWeb.publetSettings(key) match {
          case Some(filter) => {
            filter :: recurseFind(num +1)
          }
          case None => Nil
        }
      }
      recurseFind(0)
    }
  })).get

  /**
   * Returns the realm name that is used for WebDAV. This is
   * either retrieved from the settings or the value "WebDav Area"
   * is returned as fallback.
   *
   * @return
   */
  def getRealmName = PubletWeb.publetSettings("webdav.realmName").getOrElse("WebDav Area")
}
