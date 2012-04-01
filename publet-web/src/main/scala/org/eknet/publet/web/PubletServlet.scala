package org.eknet.publet.web

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import java.net.URLDecoder
import org.slf4j.LoggerFactory
import org.eknet.publet._
import source.{Partitions, FilesystemPartition}
import tools.nsc.io.File


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 27.03.12 22:42
 */
class PubletServlet extends HttpServlet {
  private val log = LoggerFactory.getLogger(getClass)

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


  protected def publet = getServletContext.getAttribute("publet") match {
    case null => sys.error("publet servlet not initialized")
    case p: Publet => p
    case _ => sys.error("wrong attribute type")
  }

  override def init() {
    val config = getServletConfig
    val publetRoot = Option(config.getInitParameter("publetRoot"))
      .getOrElse(sys.error("No publet root specified!"))

    val app = getServletContext
    if (publetRoot.startsWith(File.separator)) {
      log.info("Initialize publet root to: "+ publetRoot)
      app.setAttribute("publet", Publet.default(Path.root, new FilesystemPartition(publetRoot)))
    } else {
      val np = new java.io.File(".").getAbsolutePath+ File.separator+ publetRoot
      log.info("Initialize publet root to: "+ np)
      app.setAttribute("publet", Publet.default(Path.root, new FilesystemPartition(np)))
    }
  }

  def publetPath(req: HttpServletRequest) =  Path(URLDecoder
    .decode(Path(req.getRequestURI).strip.asString, "UTF-8"))

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    val path = publetPath(req)
    Option(req.getParameter("edit")) match {
      case None => publish(path, resp)
      case Some(_) => edit(path, resp)
    }
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    val path = publetPath(req)
    Option(req.getParameter("page")) match {
      case None =>
      case Some(body) => {
        publet.push(path, StringContent(body, ContentType.markdown))
      }
    }
    publish(path, resp)
  }
}
