package org.eknet.publet.web.webdav

import javax.servlet._
import http.HttpServletRequest
import org.eknet.publet.web.filter.HttpFilter
import com.bradmcevoy.http.{Response, Request, AuthenticationService, HttpManager}
import org.eknet.publet.web.{PubletWebContext, Config, PubletWeb}
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.util.Key
import org.apache.shiro.authz.UnauthenticatedException
import com.bradmcevoy.http.http11.auth.BasicAuthHandler

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
    if (WebdavFilter.isDavRequest) {
      import com.bradmcevoy
      val request: Request = new bradmcevoy.http.ServletRequest(req, PubletWeb.servletContext)
      val response: Response = new bradmcevoy.http.ServletResponse(resp)
      try {
        httpManager.process(request, response)
      }
      catch {
        case e:UnauthenticatedException => {
        }
      }
    } else {
      chain.doFilter(req, resp)
    }
  }
}

object WebdavFilter {

  private val webdavFilterKey = Key(getClass.getName, {
    case org.eknet.publet.web.util.Request => {
      isDavRequest(PubletWebContext.applicationPath)
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
  def isDavRequest(path: Path): Boolean = {
    if (!Config("webdav.enabled").map(_.toBoolean).getOrElse(true)) {
      false
    } else {
      getWebdavFilterUrls.exists(url => path.prefixedBy(Path(url)))
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
