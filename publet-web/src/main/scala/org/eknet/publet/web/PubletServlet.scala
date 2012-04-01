package org.eknet.publet.web

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import org.eknet.publet.source.{Partitions, FilesystemPartition}
import java.net.URLDecoder
import org.slf4j.LoggerFactory
import io.Source
import org.eknet.publet._


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 27.03.12 22:42
 */
class PubletServlet extends HttpServlet {
  private val log = LoggerFactory.getLogger(getClass)
  
  private val publet = Publet.default(Path.root, new FilesystemPartition("/home/eike/temp/publet"))

  def publish(path: Path, resp: HttpServletResponse) {
    val html = publet.process(path, path.targetType.getOrElse(ContentType.html))
    html.fold(writeError(_, path, resp), writePage(_, path, resp))
  }
  
  def edit(path: Path, resp: HttpServletResponse) {
    val html = publet.process(path, ContentType.markdown, publet.getEngine('edit).get)
    html.fold(writeError(_, path, resp), writePage(_, path, resp))
  }

  def writeError(ex: Exception, path: Path, resp: HttpServletResponse) {
    log.error("Error!", ex)
    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
//    resp.getOutputStream.println("<h3>PATH: "+ path.asString+ "</h3>")
  }

  def writePage(page: Option[Content], path: Path, resp: HttpServletResponse) {
    val out = resp.getOutputStream
    page match {
      case None => publet.create(path, ContentType.markdown) match {
        case Left(x) => writeError(x, path, resp)
        case Right(x) => edit(path, resp)
      }
      case Some(p) => p.copyTo(out)
    }
  }

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    val path = Path(URLDecoder.decode(Path(req.getRequestURI).strip.asString, "UTF-8"))
    Option(req.getParameter("edit")) match {
      case None => publish(path, resp)
      case Some(_) => edit(path, resp)
    }
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    val path = Path(URLDecoder.decode(Path(req.getRequestURI).strip.asString, "UTF-8"))
    Option(req.getParameter("page")) match {
      case None =>
      case Some(body) => {
        publet.push(path, StringContent(body, ContentType.markdown))
      }
    }
    publish(path, resp)
  }
}
