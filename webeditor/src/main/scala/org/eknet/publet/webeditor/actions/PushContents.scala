package org.eknet.publet.webeditor.actions

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.engine.scala.ScalaScript._
import org.eknet.publet.web.WebContext
import org.eknet.publet.vfs.{Path, Content, ContentType}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 19:46
 */
object PushContents extends ScalaScript {

  def serve() = {
    WebContext().parameter("path") match {
      case Some(path) => {
        val p = Path(path)
        pushBinary(p)
        pushText(p)
        WebContext().redirect(path)
        makeJson(Map("success"->true))
      }
      case None => makeJson(Map("success"->false, "message"->"No path name given."))
    }
  }

  def pushBinary(path: Path) {
    val ctx = WebContext()
    ctx.uploads.foreach(fi => {
      log.debug("Create {} file", ctx.requestPath.name.targetType)
      ctx.webPublet.publet.push(path, Content(fi.getInputStream, ctx.requestPath.name.targetType))
    })
  }


  def pushText(path: Path) {
    val ctx = WebContext()
    val publet = ctx.webPublet
    ctx.parameter("page") match {
      case None =>
      case Some(body) => {
        val target = ctx.parameter("type").getOrElse("markdown")
        log.debug("Write {} file", target)
        publet.publet.push(path, Content(body, ContentType(Symbol(target))))
      }
    }
  }
}
