package org.eknet.publet.web.filter

import org.eknet.publet.Path
import org.eknet.publet.resource.{ContentType, Content}
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletResponse
import org.eknet.publet.web.{Config, WebContext, UploadContent}
import java.io.{PrintWriter, StringWriter}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:12
 *
 */
trait PageWriter {
  private val log = LoggerFactory.getLogger(getClass)

  def writeError(ex: Throwable, resp: HttpServletResponse) {
    log.error("Error!", ex)
    if (Config.value("mode").getOrElse("development") == "development") {
      //print the exception in development mode
      val sw = new StringWriter()
      ex.printStackTrace(new PrintWriter(sw))
      val content = Content("<h2>Exception</h2><pre class='stacktrace'>"+sw.toString+ "</pre>", ContentType.html)

      val result = WebContext().publet.getEngine('mainWiki).get
        .process(WebContext().requestPath, Seq(content), ContentType.html)

      result.fold(x=>resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), c=>writePage(Some(c), resp))
    } else {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
    }
  }

  def writePage(page: Option[Content], resp: HttpServletResponse) {
    val out = resp.getOutputStream
    val path = WebContext().requestPath
    page match {
      case None => createNew(path, resp)
      case Some(p) => resp.setContentType(p.contentType.mimeString); p.copyTo(out)
    }
  }

  def createNew(path: Path, resp: HttpServletResponse) {
    val out = resp.getOutputStream
    val targetType = path.targetType.get
    val publet = WebContext().publet
    if (targetType.mime._1 == "text") {
      publet.getEngine('edit).get.process(path, Seq(Content("", ContentType.markdown)), ContentType.markdown) match {
        case Left(x) => writeError(x, resp)
        case Right(x) => x.copyTo(out)
      }
    } else {
      val uploadContent = UploadContent.uploadContent(path)
      publet.resolveEngine(path).get.process(path, Seq(uploadContent), ContentType.html)
      .fold(writeError(_, resp), c => writePage(Some(c), resp))
    }
  }

}
