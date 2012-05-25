package org.eknet.publet.webeditor.actions

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.shiro.Security
import java.io.ByteArrayInputStream
import grizzled.slf4j.Logging
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.{GitAction, PubletWeb, PubletWebContext}
import org.eknet.publet.web.util.RenderUtils
import org.eknet.publet.webeditor.EditorPaths

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 19:46
 */
object PushContents extends ScalaScript with Logging {

  def notify(success: Boolean, msg: String, head: Option[String] = None) = {
    ScalaScript.makeJson(Map("success"->success, "message"->msg, "lastMod"->head.getOrElse("")))
  }

  def serve() = {
    val ctx = PubletWebContext
    ctx.param("delete") match {
      case Some(path) => {
        try {
          delete(Path(path))
          PubletWebContext.redirect(EditorPaths.editHtmlPage.asString+"?resource="+path)
          notify(success = true, msg = "File deleted!")
        } catch {
          case e:Exception => RenderUtils.renderMessage("Error", "Error while deleting.", "error")
        }
      }
      case _=> ctx.param("path") match {
        case Some(path) => {
          val p = Path(path)
          try {
            pushText(p)
            notify(success = true, msg = "Successfully saved.", head = getHead(p))
          } catch {
            case e:Exception => {
              error("Error while saving file!", e)
              notify(success = false, msg = e.getMessage)
            }
          }
        }
        case None => notify(success = false, msg = "Not enough arguments!")
      }
    }
  }

  def delete(path: Path) {
    Security.checkGitAction(GitAction.push)
    info("Deleting now: "+ path)
    PubletWeb.publet.delete(path)
  }

  def pushText(path: Path) {
    val ctx = PubletWebContext
    val publet = PubletWeb.publet
    ctx.param("page") match {
      case None =>
      case Some(body) => {
        Security.checkGitAction(GitAction.push)
        val target = ctx.param("extension").getOrElse("md")
        val commitMsg = ctx.param("commitMessage").filter(!_.isEmpty)
        val oldhead = ctx.param("head").getOrElse("")
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
    PubletWeb.publet.findSources(path).toList match {
      case d::ds => d.lastModification.map(_.toString)
      case _ => None
    }
  }
}
