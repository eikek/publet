package org.eknet.publet.web

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import java.net.URLDecoder
import org.slf4j.LoggerFactory
import org.eknet.publet._
import resource.FilesystemPartition
import scala.collection.JavaConversions._
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import java.io.File


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
    if (path.targetType.get.mime._1 == "text") {
      val html = publet.process(path, ContentType.markdown, publet.getEngine('edit).get)
      html.fold(writeError(_, path, resp), writePage(_, path, resp))
    } else {
      createNew(path, resp)
    }
  }

  def writeError(ex: Exception, path: Path, resp: HttpServletResponse) {
    log.error("Error!", ex)
    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
  }

  def writePage(page: Option[Content], path: Path, resp: HttpServletResponse) {
    val out = resp.getOutputStream
    page match {
      case None => createNew(path, resp)
      case Some(p) => p.copyTo(out)
    }
  }

  def createNew(path: Path, resp: HttpServletResponse) {
    val out = resp.getOutputStream
    val targetType = path.targetType.get
    if (targetType.mime._1 == "text") {
      publet.getEngine('edit).get.process(path, Seq(StringContent("", ContentType.markdown)), ContentType.markdown) match {
        case Left(x) => writeError(x, path, resp)
        case Right(x) => x.copyTo(out)
      }
    } else {
      publet.getEngine('upload).get.process(path, Seq(StringContent("", targetType)), targetType) match {
        case Left(x) => writeError(x, path, resp)
        case Right(x) => x.copyTo(out)
      }
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
      app.setAttribute("publet", Publet.default(Path.root, new FilesystemPartition(publetRoot, 'publetroot)))
    } else {
      val np = new java.io.File(".").getAbsolutePath+ File.separator+ publetRoot
      log.info("Initialize publet root to: "+ np)
      app.setAttribute("publet", Publet.default(Path.root, new FilesystemPartition(np, 'publetroot)))
    }
//    publet.register("/index.html", PassThrough)
  }

  def publetPath(req: HttpServletRequest) = {
    val p = Path(URLDecoder
      .decode(Path(req.getRequestURI).strip.asString, "UTF-8"))
    if (p.isRoot) (p / Path("index.html")) else p
  }

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
        val target = Option(req.getParameter("type")).getOrElse("markdown")
        log.debug("Create {} file", target)
        publet.push(path, StringContent(body, ContentType(Symbol(target))))
      }
    }
    uploads(req) match {
      case List() =>
      case list => {
        val target = path.targetType.get
        list.foreach(fi => {
          log.debug("Create {} file", target)
          publet.push(path, StreamContent(fi.getInputStream, target))
        })
      }
    }

    Option(req.getParameter("file")) match {
      case None =>
      case Some(data) => {

      }
    }
    publish(path, resp)
  }

  private def uploads(req: HttpServletRequest): List[FileItem] = {
    if (req.getContentType.startsWith("multipart")) {
      val items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
      items.collect({case p:FileItem => p}).filter(!_.isFormField).toList
    } else {
      List[FileItem]()
    }
  }
}
