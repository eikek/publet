package org.eknet.publet.web.filter

import org.eknet.publet.vfs.ContentType
import javax.servlet.http.{HttpServlet, HttpServletResponse, HttpServletRequest}
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web.{WebPublet, WebContext}

/** Servlet that processes the resource using the engine
 * as specified with `a=` http request parameter or the
 * default engine.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:03
 *
 */
class PublishServlet extends HttpServlet with PageWriter {

  override def service(req: HttpServletRequest, resp: HttpServletResponse) {
    WebContext().action match {
      case Some(engine) => processEngine(req, resp, engine);
      case _ => processDefault(req, resp)
    }
  }

  def processEngine(req: HttpServletRequest, resp: HttpServletResponse, engine: String) {
    val engineId = Symbol(engine)
    val path = WebContext().requestPath
    val publet = WebPublet().publet
    val targetType = path.name.targetType
    if (!Security.isAnonymousRequest) {
      Security.checkPerm(engineId, path)
    }
    val html = publet.process(path, targetType, publet.engineManager.getEngine(engineId).get)
    writePage(html, resp)
  }

  def processDefault(req: HttpServletRequest, resp: HttpServletResponse) {
    val publet = WebPublet().publet
    val path = WebContext().requestPath
    if (!Security.isAnonymousRequest) {
      Security.checkPerm(Security.get, path)
    }
    val tt = if (path.name.targetType == ContentType.unknown) ContentType.html else path.name.targetType
    val html = publet.process(path, tt)
    writePage(html, resp)
  }
}
