package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import javax.servlet._
import org.eknet.publet.web.{PubletWeb, PubletWebContext}
import org.eknet.publet.vfs.{Path, ContentType}

/** Servlet that processes the resource using the engine
 * as specified with `a=` http request parameter or the
 * default engine.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:03
 *
 */
class PubletFilter extends Filter with PageWriter with HttpFilter {


  def init(filterConfig: FilterConfig) {}

  def destroy() {}

  def doFilter(req: ServletRequest, resp: ServletResponse, chain: FilterChain) {
    PubletWebContext.param("a") match {
      case Some(engine) => processEngine(req, resp, engine);
      case _ => processDefault(req, resp)
    }
  }

  def getAppPath(req:HttpServletRequest) = {
    val p = Path(req.getRequestURI.substring(req.getContextPath.length))
    if (p.directory) p/"index.html" else p
  }

  def processEngine(req: HttpServletRequest, resp: HttpServletResponse, engine: String) {
    val engineId = Symbol(engine)
    val path = getAppPath(req)

    val publet = PubletWeb.publet
    val targetType = path.name.targetType
    val html = publet.process(path, targetType, publet.engineManager.getEngine(engineId).get)
    writePage(html, resp)
  }

  def processDefault(req: HttpServletRequest, resp: HttpServletResponse) {
    val publet = PubletWeb.publet
    val path = getAppPath(req)

    val tt = if (path.name.targetType == ContentType.unknown) ContentType.html else path.name.targetType
    val html = publet.process(path, tt)
    writePage(html, resp)
  }
}
