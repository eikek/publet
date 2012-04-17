package org.eknet.publet.web.extensions.scripts

import org.eknet.publet.engine.scalascript.ScalaScript
import org.eknet.publet.web.WebContext
import org.eknet.publet.Path
import org.eknet.publet.resource._
import ScalaScript._

/** TODO replace with javascript browser...
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 17.04.12 22:44
 */
object Listing extends ScalaScript {

  def anchor(path: Path, container: Boolean): String = {
    val ref = if (container) path.asString else path.withExtension("html").asString
    "<a href=\""+ ref +"\">"+ path.fileName.toString +(if (container) "/" else "") +"</a>"
  }

  def list(res: Resource, depth: Int, maxd: Int, start: Path): String = {
    if (depth >= maxd) ""
    else {
      res match {
        case cr: ContainerResource if (!hidden(cr.path)) => {
          (if (cr.path != start) "<li>"+ anchor(cr.path, true) +"</li>\n" else "") +
          (if (!cr.children.isEmpty) "<ul>" + (for (c<-cr.children) yield {
              list(c, depth+1, maxd, start)
            }).mkString("\n") +"</ul>" else "")
        }
        case c if (!hidden(c.path)) => "<li>"+ anchor(res.path, false) +"</li>"
        case _ => ""
      }
    }
  }

  def hidden(path: Path): Boolean = path.asString.startsWith("/.")

  def serve() = {
    val ctx = WebContext()
    import ctx._

    val maxdepth = parameter("maxd").getOrElse("2").toInt
    val startpath = Path(parameter("start").getOrElse("/"))
    makeHtml {
      "<h3><pre>"+ startpath.asString +"</pre></h3>\n" +
      list(publet.lookup(startpath).get, 0, maxdepth, startpath)
    }
  }
}
