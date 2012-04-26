package org.eknet.publet.webeditor.actions

import org.eknet.publet.engine.scalascript.ScalaScript
import ScalaScript._
import org.eknet.publet.web.WebContext
import org.slf4j.LoggerFactory
import org.eknet.publet.vfs.Path

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.04.12 19:17
 */
object SetEngine extends ScalaScript {
  private val log = LoggerFactory.getLogger(getClass)

  def serve() = {
    params("path", "publetEngine") match {
      case None => error("Arguments missing")
      case Some(t) => {
        val publet = WebContext().webPublet.publet
        publet.engineManager.getEngine(Symbol(t._2)) match {
          case None => error("Engine '" + t._2 + "' not found.")
          case Some(pe) => {
            val path = WebContext.calcPath(Path(t._1))
            log.info("Registering engine " + pe.name + " with url: " + path.asString)
            publet.engineManager.register(path.asString, pe)
            success()
          }
        }
      }
    }
  }

  private def params(p1: String, p2: String): Option[(String, String)] = {
    WebContext().parameter(p1) match {
      case None => None
      case Some(a1) => WebContext().parameter(p2) match {
        case None => None
        case Some(a2) => Some((a1, a2))
      }
    }
  }

  private def success() = makeJson(Map("success" -> true, "message" -> "Successfully set engine."))

  private def error(str: String) = makeJson(Map("success" -> false, "message" -> str))
}
