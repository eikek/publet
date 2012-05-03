package org.eknet.publet.web.filter

import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletResponse
import java.io.{PrintWriter, StringWriter}
import org.eknet.publet.vfs._
import org.eknet.publet.engine.convert.CodeHtmlConverter
import util.SimpleContentResource
import org.eknet.publet.web.shiro.Security
import org.apache.shiro.authz.{UnauthenticatedException, AuthorizationException}
import org.eknet.publet.web.{WebPublet, WebContext, Config}
import scala.Some

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:12
 *
 */
trait PageWriter {
  private val log = LoggerFactory.getLogger(getClass)

  def writeUnauthorizedError(resp:HttpServletResponse) {
    val publet = WebPublet().publet
    val r401 = publet.process(Path(Config.mainMount+"/.allIncludes/401.html").toAbsolute)
    if (r401.isDefined) {
      val resource = new SimpleContentResource(WebContext().requestPath.name, r401.get)
      val result = publet.engineManager.getEngine('include).get
        .process(WebContext().requestPath, Seq(resource), ContentType.html)
      writePage(result, resp)

    } else {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED)
    }
  }

  def writeError(ex: Throwable, resp: HttpServletResponse) {
    log.error("Error!", ex)
    val publet = WebPublet().publet
    if (ex.getCause.isInstanceOf[AuthorizationException]) {
      if (ex.getCause.isInstanceOf[UnauthenticatedException]) {
        Security.redirectToLoginPage()
      } else {
        writeUnauthorizedError(resp)
      }
    }
    else if (Config("mode").getOrElse("development") == "development") {
      //print the exception in development mode
      val sw = new StringWriter()
      ex.printStackTrace(new PrintWriter(sw))
      val content = Content("<h2>Exception</h2><pre class='stacktrace'>"+CodeHtmlConverter.replaceChars(sw.toString)+ "</pre>", ContentType.html)
      val resource = new SimpleContentResource(WebContext().requestPath.name, content)
      val result = publet.engineManager.getEngine('mainWiki).get
        .process(WebContext().requestPath, Seq(resource), ContentType.html)

      writePage(result, resp)
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
    WebContext().service(WebContext.notFoundHandlerKey).resourceNotFound(path, resp)
  }

}
