package org.eknet.publet.web

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import org.eknet.publet.{Content, ContentType, Path, Publet}
import org.eknet.publet.source.{Partitions, FilesystemPartition}
import org.eknet.publet.postproc.YamlTemplate


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 27.03.12 22:42
 */
class PubletServlet extends HttpServlet {

  private val publet = Publet.default(Path.root, new FilesystemPartition("/tmp/publet"))
  publet.install(Path.root, YamlTemplate)

  def action(req: HttpServletRequest, resp: HttpServletResponse) {
    
    val path = Path(req.getRequestURI).strip
    val html = publet.process(path, path.targetType.getOrElse(ContentType.html))

    html.fold(writeError(_, path, resp), writePage(_, path, resp))
  }

  def writeError(ex: Exception, path: Path, resp: HttpServletResponse) {
    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
//    resp.getOutputStream.println("<h3>PATH: "+ path.asString+ "</h3>")
  }

  def writePage(page: Option[Content], path: Path, resp: HttpServletResponse) {
    val out = resp.getOutputStream
    page match {
      case None => resp.sendError(HttpServletResponse.SC_NOT_FOUND)
      case Some(p) => out.println(p.contentAsString)
    }
  }

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    action(req, resp)
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    action(req, resp)
  }
}
