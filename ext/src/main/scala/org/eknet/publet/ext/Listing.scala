package org.eknet.publet.ext

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.vfs._
import ScalaScript._
import org.eknet.publet.web.{PubletWebContext, PubletWeb, Config}

/**TODO replace with javascript browser...
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 17.04.12 22:44
 */
object Listing extends ScalaScript {

  def anchor(path: Path, res: Resource): String = {
    val container = Resource.isContainer(res)
    val ref =  PubletWeb.servletContext.getContextPath +
      (if (container) path.asString else path.withExt("html").asString)

    "<a href=\"" + ref + "\">" + path.name.fullName + (if (container) "/" else "") + "</a>"
  }

  def list(path: Path, res: Resource, depth: Int, maxd: Int): String = {
    if (depth >= maxd) ""
    else {
      res match {
        case cr: ContainerResource if (!hidden(cr.name.fullName)) => {
          (if (depth > 0) "<li>" + anchor(path/cr.name, cr) + "</li>\n" else "") +
            (if (!cr.children.isEmpty) "<ul>" + (for (c <- cr.children) yield {
              list(path/cr.name, c, depth + 1, maxd)
            }).mkString("\n") + "</ul>"
            else "")
        }
        case c if (!hidden(c.name.fullName)) => "<li>" + anchor(path/c.name, res) + "</li>"
        case _ => ""
      }
    }
  }

  def hidden(name: String): Boolean = name.startsWith(".")

  def serve() = {
    val ctx = PubletWebContext
    import ctx._

    val maxdepth = param("maxd").getOrElse("1").toInt
    val startpath = Path(param("start").getOrElse("/"+ Config.mainMount))
    makeHtml {
      "<h3><pre>" + startpath.asString + "</pre></h3>\n" +
        list(startpath.parent, PubletWeb.publet.rootContainer.lookup(startpath).get, 0, maxdepth)
    }
  }
}
