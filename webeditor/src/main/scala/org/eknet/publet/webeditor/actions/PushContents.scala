package org.eknet.publet.webeditor.actions

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.engine.scala.ScalaScript._
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.{WebContext, WebPublet}
import org.eknet.publet.web.shiro.Security
import java.io.ByteArrayInputStream
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 19:46
 */
object PushContents extends ScalaScript with Logging {

  def serve() = {
    WebContext().parameter("delete") match {
      case Some(path) => {
        delete(Path(path))
        WebContext().redirect(Path(path).withExt("html").asString)
        None
      }
      case _=> WebContext().parameter("path") match {
        case Some(path) => {
          val p = Path(path)
          pushBinary(p)
          pushText(p)
          makeHtml("<script>\n$(document).ready(function()\n  {\n    $.sticky('<b>Successfully saved!</b>');\n  });\n</script>")
        }
        case None => makeHtml("<script>\n$(document).ready(function()\n  {\n    $.sticky('<b>Error while saving!</b>');\n  });\n</script>")
      }
    }
  }

  def pushBinary(path: Path) {
    val ctx = WebContext()
    ctx.uploads.foreach(fi => {
      Security.checkPerm(Security.put, path)
      log.debug("Create {} file", ctx.requestPath.name.targetType)
      WebPublet().publet.push(path, fi.getInputStream)
    })
  }

  def delete(path: Path) {
    info("Deleting now: "+ path)
    WebPublet().publet.delete(path)
  }

  def pushText(path: Path) {
    val ctx = WebContext()
    val publet = WebPublet().publet
    ctx.parameter("page") match {
      case None =>
      case Some(body) => {
        Security.checkPerm(Security.put, path)
        val target = ctx.parameter("extension").getOrElse("md")
        val commitMsg = ctx.parameter("commitMessage").filter(!_.isEmpty)
        log.debug("Write {} file", target)
        publet.push(path.withExt(target), new ByteArrayInputStream(body.getBytes("UTF-8")))
      }
    }
  }
}
