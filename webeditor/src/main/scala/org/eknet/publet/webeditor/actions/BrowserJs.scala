package org.eknet.publet.webeditor.actions

import org.eknet.publet.engine.scala.ScalaScript
import ScalaScript._
import io.Source
import org.eknet.publet.web.WebContext
import org.eknet.publet.webeditor.EditorWebExtension

/** Replaces all absolute urls by prefixing the context path.
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 22:08
 */
object BrowserJs extends ScalaScript {

  val pathRegex = ("\"("+ EditorWebExtension.editorPath.asString +"[^\"]*\")").r

  def serve() = {
    val file = getClass.getResource("../browser_templ.js")
    val cp = WebContext.getContextPath()
    if (!cp.isDefined) {
      makeJs(Source.fromURL(file, "UTF-8").getLines().mkString("\n"))
    } else {
      makeJs(Source.fromURL(file, "UTF-8").getLines().map(line =>
        pathRegex.findFirstMatchIn(line) match {
          case Some(m) => m.before +"\""+ cp.get + m.group(1) + m.after
          case None => line
        }
      ).mkString("\n"))
    }
  }
}
