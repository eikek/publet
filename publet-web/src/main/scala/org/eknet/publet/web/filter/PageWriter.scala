package org.eknet.publet.web.filter

import org.eknet.publet.Path
import org.eknet.publet.resource.{ContentType, Content}
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.eknet.publet.web.{WebContext, UploadContent}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:12
 *
 */
trait PageWriter {
  private val log = LoggerFactory.getLogger(getClass)

  def writeError(ex: Exception, path: Path, resp: HttpServletResponse) {
    log.error("Error!", ex)
    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
  }

  def writePage(page: Option[Content], path: Path, req: HttpServletRequest, resp: HttpServletResponse) {
    val out = resp.getOutputStream
    page match {
      case None => createNew(path, req, resp)
      case Some(p) => resp.setContentType(p.contentType.mimeString); p.copyTo(out)
    }
  }

  def createNew(path: Path, req: HttpServletRequest, resp: HttpServletResponse) {
    val out = resp.getOutputStream
    val targetType = path.targetType.get
    val publet = WebContext().publet
    if (targetType.mime._1 == "text") {
      publet.getEngine('edit).get.process(path, Seq(Content("", ContentType.markdown)), ContentType.markdown) match {
        case Left(x) => writeError(x, path, resp)
        case Right(x) => x.copyTo(out)
      }
    } else {
      val uploadContent = UploadContent.uploadContent(path)
      publet.resolveEngine(path).get.process(path, Seq(uploadContent), ContentType.html)
      .fold(writeError(_, path, resp), c => writePage(Some(c), path, req, resp))
    }
  }

}
