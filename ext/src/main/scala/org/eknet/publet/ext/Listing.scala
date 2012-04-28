package org.eknet.publet.ext

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.vfs._
import ScalaScript._
import org.eknet.publet.web.{Config, WebContext}

/**TODO replace with javascript browser...
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 17.04.12 22:44
 */
object Listing extends ScalaScript {

  def anchor(path: Path, container: Boolean): String = {
    val publet = WebContext().service(WebContext.webPubletKey).publet
    val mountPath = publet.mountManager.resolveMount(path).map(_._1.asString).getOrElse("")
    val ref =  WebContext().getContextPath.getOrElse("") +
      (if (container) path.asString else path.withExt("html").asString)

    "<a href=\"" + mountPath+ref + "\">" + path.name.fullName + (if (container) "/" else "") + "</a>"
  }

  def list(res: Resource, depth: Int, maxd: Int, start: Path): String = {
    if (depth >= maxd) ""
    else {
      res match {
        case cr: ContainerResource if (!hidden(cr.path)) => {
          (if (depth > 0) "<li>" + anchor(cr.path, true) + "</li>\n" else "") +
            (if (!cr.children.isEmpty) "<ul>" + (for (c <- cr.children) yield {
              list(c, depth + 1, maxd, start)
            }).mkString("\n") + "</ul>"
            else "")
        }
        case c if (!hidden(c.path)) => "<li>" + anchor(res.path, false) + "</li>"
        case _ => ""
      }
    }
  }

  def hidden(path: Path): Boolean = path.asString.startsWith("/.")

  def serve() = {
    val ctx = WebContext()
    import ctx._

    val maxdepth = parameter("maxd").getOrElse("2").toInt
    val startpath = Path(parameter("start").getOrElse("/"+ Config.mainMount))
    makeHtml {
      "<h3><pre>" + startpath.asString + "</pre></h3>\n" +
        list(webPublet.publet.rootContainer.lookup(startpath).get, 0, maxdepth, startpath)
    }
  }
}
