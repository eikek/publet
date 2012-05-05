package org.eknet.publet.webeditor.actions

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.{WebContext, WebPublet}
import org.eknet.publet.web.shiro.Security
import java.io.ByteArrayInputStream
import grizzled.slf4j.Logging
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.template.Javascript

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 19:46
 */
object PushContents extends ScalaScript with Logging with Javascript {

  def notify(level:String, msg: String, head: Option[String]) = {
    val m = message(msg, Some(level))
    val headchange = head.map("$('#lastHead').attr('value', '"+ _ + "');").getOrElse("")
    jsFunction(m+"\n"+ headchange)
  }

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
          try {
            pushBinary(p)
            pushText(p)
            notify("success", "Successfully saved.", getHead(p))
          } catch {
            case e:Exception => {
              error("Error while saving file!", e)
              notify("error", e.getMessage, None)
            }
          }
        }
        case None => notify("error", "Error while saving!", None)
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
        val oldhead = ctx.parameter("head").getOrElse("")
        val newhead = getHead(path).getOrElse("")
        if (oldhead != newhead) {
          sys.error("The repository has changed since you started editing!")
        }
        log.debug("Write {} file", target)
        publet.push(path.withExt(target), new ByteArrayInputStream(body.getBytes("UTF-8")), commitMsg)
      }
    }
  }

  def getHead(path: Path) = {
    WebPublet().gitPartition.lastCommit(path.strip).map(_.getId.name())
  }
}
