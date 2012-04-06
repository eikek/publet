package org.eknet.publet.web.filter

import scala.collection.JavaConversions._
import javax.servlet.http.HttpServletRequest
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.eknet.publet.Path._
import java.net.URLDecoder
import org.eknet.publet.{Path, Publet}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 14:20
 *
 */
trait FilterContext {

  val actionAttribute = "a"

  def action(req: HttpServletRequest) = Option(req.getParameter(actionAttribute))

  def servletContext(implicit req: HttpServletRequest) = req.getSession.getServletContext

  def publet(req: HttpServletRequest) = servletContext(req).getAttribute("publet") match {
    case null => sys.error("Servlet not setup")
    case p: Publet => p
    case _ => sys.error("wrong attribute type")
  }

  def uploads(req: HttpServletRequest): List[FileItem] = {
    val rct = req.getContentType
    if (rct != null && rct.startsWith("multipart")) {
      val items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
      items.collect({case p:FileItem => p}).filter(!_.isFormField).toList
    } else {
      List[FileItem]()
    }
  }

  def path(req: HttpServletRequest) = {
    val p = Path(URLDecoder
      .decode(Path(req.getRequestURI).strip.asString, "UTF-8"))
    if (p.isRoot) (p / Path("index.html")) else p
  }
}
