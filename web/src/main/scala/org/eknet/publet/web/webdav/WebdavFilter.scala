package org.eknet.publet.web.webdav

import javax.servlet._
import org.eknet.publet.web.filter.HttpFilter
import org.eknet.publet.web.{Method, PubletWebContext, Config, PubletWeb}
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.util.Key
import org.apache.shiro.authz.UnauthenticatedException
import io.milton.http.{HttpManager, Response, Request}
import io.milton.config.HttpManagerBuilder
import io.milton.servlet.{MiltonServlet, DefaultMiltonConfigurator}
import java.util

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 21:02
 */
class WebdavFilter extends Filter with HttpFilter {

  private val configurator = new DefaultMiltonConfigurator {
    override def build() {
      builder.setEnableFormAuth(false)
      super.build()
    }
  }
  private var servletContext:ServletContext = null
  private var httpManager: HttpManager = null

  def init(filterConfig: FilterConfig) {
    this.servletContext = filterConfig.getServletContext
    val config = new io.milton.servlet.Config {

      def initParamNames() = new util.Enumeration[String] {
        val iter = List("resource.factory.class").iterator
        def hasMoreElements = iter.hasNext
        def nextElement() = iter.next()
      }

      def getServletContext = servletContext

      def getInitParameter(string: String) = if ("resource.factory.class" == string) classOf[WebdavResourceFactory].getName else null
    }
    this.httpManager = configurator.configure(config)
  }

  def destroy() {
    if (configurator != null)
      configurator.shutdown()
  }

  def doFilter(req: ServletRequest, resp: ServletResponse, chain: FilterChain) {
    if (WebdavFilter.isDavRequest) {
      import io.milton
      try {
        MiltonServlet.setThreadlocals(req, resp)
        val request: Request = new milton.servlet.ServletRequest(req, servletContext)
        val response: Response = new milton.servlet.ServletResponse(resp)
        httpManager.process(request, response)
      } finally {
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

}
