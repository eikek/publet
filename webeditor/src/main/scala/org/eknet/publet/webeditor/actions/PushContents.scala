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
    val ctx = WebContext()
    ctx.parameter("delete") match {
      case Some(path) => {
        try {
          delete(Path(path))
          ctx.redirect(Path(path).withExt("html").asString)
          None
        } catch {
          case e:Exception => notify("error", "Error while deleting.", None)
        }
      }
      case _=> ctx.parameter("path") match {
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
        case None => notify("error", "Not enough arguments!", None)
      }
    }
  }

  def pushBinary(path: Path) {
    val ctx = WebContext()
    ctx.uploads.foreach(fi => {
      Security.checkPerm(Security.put, path)
      WebPublet().publet.push(path, fi.getInputStream)
    })
  }

  def delete(path: Path) {
    Security.checkPerm(Security.delete, path)
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
        debug("Write "+target+" file")
        publet.push(path.withExt(target), new ByteArrayInputStream(body.getBytes("UTF-8")), commitMsg)
      }
    }
  }

  def getHead(path: Path) = {
    WebPublet().publet.rootContainer.lookup(path).flatMap(_.lastModification).map(_.toString)
  }
}
