package org.eknet.publet.web.filter

import javax.servlet._
import http.{HttpServletRequest, HttpServletRequestWrapper}
import org.eknet.publet.web.{Config, PubletWeb}
import org.eknet.publet.vfs.Path

/**
 * Does the routing of the request through a simple filter chain.
 *
 * This is the main entry point into publet web app and the only
 * filter that needs to be configured in `web.xml`.
 *
 * If the request starts with the git path as configured in
 * the config file, then the git-servlet chain is invoked.
 * Otherwise the request is handed to the publet filter.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 21:22
 */
class RoutingFilter extends Filter with HttpFilter {

  /**
   * Filter that get executed with any request
   * in the following order
   */
  val mainfilters = List(
    new WebContextFilter,
    new RedirectFilter,
    new PubletShiroFilter,
    new AuthzFilter
  )

  // the two main filters executed at the end
  val publetFilter = new PubletFilter
  val gitFilter = new GitHttpFilter(PubletWeb.gitr)

  class MyFilterChain(filters: List[Filter], chain:FilterChain) extends FilterChain {

    def doFilter(request: ServletRequest, response: ServletResponse) {
      filters match {
        case f::fs => f.doFilter(request, response, new MyFilterChain(fs, chain))
        case Nil => {
          val utils = getRequestUtils(request)
          if (utils.isGitRequest) {
            val req = new PathInfoServletReq(request)
            gitFilter.doFilter(req, response, chain)
          } else {
            publetFilter.doFilter(request, response, chain)
          }
        }
      }
    }
  }

  def init(filterConfig: FilterConfig) {
    (publetFilter :: gitFilter :: mainfilters) foreach (_.init(filterConfig))
  }

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    new MyFilterChain(mainfilters, chain).doFilter(request, response)
  }

  def destroy() {
    (publetFilter :: gitFilter :: mainfilters) foreach (_.destroy())
  }

}

private class PathInfoServletReq(req: HttpServletRequest) extends HttpServletRequestWrapper(req) {

  val gitMount = Config.gitMount

  override def getPathInfo = {
    getRequestURI.substring(gitMount.length+1)
      //+ (if (getQueryString != null) "?"+getQueryString else "")
  }

  override def getServletPath = Path(gitMount).toAbsolute.asString
}