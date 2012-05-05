package org.eknet.publet.web.template

import org.eknet.publet.engine.scala.ScalaScript._

/**
 * Generates `<script/>` html.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.05.12 02:17
 */
trait Javascript {

  def message(msg: String, level: Option[String]) = {
    val buf = new StringBuilder()
    buf.append("$.sticky('<p>"+msg+"</p>'")
    level.foreach(level => buf.append(", { level: '"+level+"' }"))
    buf.append(");")
    buf.toString()
  }

  def jsFunction(fun:String) = {
    val buf = new StringBuilder()
    buf.append("<script>$(document).ready(function() {\n")
    buf.append(fun)
    buf.append("\n});</script>")
    makeHtml(buf.toString())
  }
}
