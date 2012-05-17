package org.eknet.publet.web

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import util.{Key, AttributeMap}


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 20:35
 */
object PubletWebContext extends RequestParams with RequestUrl with RepositoryNameResolver {

  private case class Cycle(req: HttpServletRequest, res: HttpServletResponse)
  private val threadCycle = new ThreadLocal[Cycle]

  private[web] def setup(req: HttpServletRequest, res: HttpServletResponse) {
    threadCycle.set(Cycle(req, res))
  }

  private[web] def clear() {
    threadCycle.remove()
  }

  // ~~~

  private def cycle = Option(threadCycle.get()).getOrElse(sys.error("WebContext not initialized"))
  protected def req = cycle.req
  protected def res = cycle.res


  def sessionMap = AttributeMap(req.getSession)
  def requestMap = AttributeMap(req)
  def contextMap = PubletWeb.contextMap

  def attr[T: Manifest](key: Key[T]) = {
    requestMap.get(key).orElse {
      sessionMap.get(key).orElse {
        contextMap.get(key)
      }
    }
  }

  def redirect(uri: String) {
    res.sendRedirect(uri)
  }

  /**
   * Redirects to the login page adding the full url of
   * the current request as parameter with name `redirect`
   */
  def redirectToLoginPage() {
    val p = params.map(t => t._1 +"="+ t._2.mkString(",")).mkString("&")
    redirect(PubletWeb.getLoginPath+"?redirect="+requestUri+"?"+p)
  }

}
