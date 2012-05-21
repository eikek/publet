package org.eknet.publet.web.filter

import javax.servlet.http.HttpServletResponse
import java.io.{PrintWriter, StringWriter}
import org.eknet.publet.vfs._
import util.SimpleContentResource
import scala.Some
import grizzled.slf4j.Logging
import org.eknet.publet.web.{PubletWebContext, PubletWeb, Config}
import org.eknet.publet.Publet

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:12
 *
 */
trait PageWriter extends Logging {

  def writeUnauthorizedError(resp:HttpServletResponse) {
    val publet = PubletWeb.publet
    val r401 = publet.process(Path(Config.mainMount+"/"+Publet.allIncludes+"401.html").toAbsolute)
    if (r401.isDefined) {
      val resource = new SimpleContentResource(ResourceName("401.html"), r401.get)
      val result = publet.engineManager.getEngine('include).get
        .process(PubletWebContext.applicationPath, resource, ContentType.html)
      writePage(result, resp)

    } else {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED)
    }
  }

  def writeInternalError(resp: HttpServletResponse) {
    val publet = PubletWeb.publet
    val path500 = Path(Config.mainMount+"/"+Publet.allIncludes+"500.html").toAbsolute
    val r500 = publet.process(path500)
    if (r500.isDefined) {
      val resource = new SimpleContentResource(ResourceName("500.html"), r500.get)
      val result = publet.engineManager.getEngine('include).get
        .process(PubletWebContext.applicationPath, resource, ContentType.html)
      writePage(result, resp)
    } else {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
    }
  }

  def writeError(ex: Throwable, resp: HttpServletResponse) {
    val publet = PubletWeb.publet
    if (Config("publet.mode").getOrElse("development") == "development") {
      //print the exception in development mode
      val sw = new StringWriter()
      ex.printStackTrace(new PrintWriter(sw))
      val content = Content("<h2>Exception</h2><pre class='stacktrace'>"+ sw.toString.replace("<", "&lt;").replace(">", "&gt;")+ "</pre>", ContentType.html)
      val resource = new SimpleContentResource(PubletWebContext.applicationPath.name, content)
      val result = publet.engineManager.getEngine('mainWiki).get
        .process(PubletWebContext.applicationPath, resource, ContentType.html)

      writePage(result, resp)
    } else {
      writeInternalError(resp)
    }
  }

  def writePage(page: Option[Content], resp: HttpServletResponse) {
    val out = resp.getOutputStream
    val path = PubletWebContext.applicationPath
    page match {
      case None => createNew(path, resp)
      case Some(p) => {
        resp.setContentType(p.contentType.mimeString)
        p.copyTo(out)
      };
    }
  }

  def createNew(path: Path, resp: HttpServletResponse) {
    PubletWeb.notFoundHandler.resourceNotFound(path, resp)
  }

}
