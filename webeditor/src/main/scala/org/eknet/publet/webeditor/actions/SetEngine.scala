package org.eknet.publet.webeditor.actions

import org.eknet.publet.engine.scala.ScalaScript
import ScalaScript._
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web.{PubletWebContext, PubletWeb}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.04.12 19:17
 */
@deprecated
object SetEngine extends ScalaScript {

  def serve() = {
    params("path", "publetEngine") match {
      case None => error("Arguments missing")
      case Some(t) => {
        val publet = PubletWeb.publet
        publet.engineManager.getEngine(Symbol(t._2)) match {
          case None => error("Engine '" + t._2 + "' not found.")
          case Some(pe) => {
            val path = Path(t._1)
            Security.checkPerm("edit", path)
            info("Registering engine " + pe.name + " with url: " + path.asString)
            publet.engineManager.register(path.asString, pe)
            success()
          }
        }
      }
    }
  }

  private def params(p1: String, p2: String): Option[(String, String)] = {
    PubletWebContext.param(p1) match {
      case None => None
      case Some(a1) => PubletWebContext.param(p2) match {
        case None => None
        case Some(a2) => Some((a1, a2))
      }
    }
  }

  private def success() = makeJson(Map("success" -> true, "message" -> "Successfully set engine."))

  private def error(str: String) = makeJson(Map("success" -> false, "message" -> str))
}
