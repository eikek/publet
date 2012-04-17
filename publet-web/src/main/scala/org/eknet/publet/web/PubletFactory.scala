package org.eknet.publet.web

import org.eknet.publet.Publet
import template._
import org.eknet.publet.engine.scalascript.ScalaScriptEvalEngine
import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.web.PubletFactory.WebScalaScriptEngine

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 23:15
 */
object PubletFactory {

  def createPublet(): Publet = {
    val publ = Publet()

    val defaultEngine = new DefaultEngine(publ)
    publ.register("/*", defaultEngine)
    publ.addEngine(defaultEngine.convEngine)

    val editEngine = new HtmlTemplateEngine('edit, EditEngine) with FilebrowserTemplate
    publ.addEngine(editEngine)

    val scalaEngine = new WebScalaScriptEngine('eval, defaultEngine)
    publ.addEngine(scalaEngine)

    val scriptInclude = new WebScalaScriptEngine('evalinclude, defaultEngine.convEngine)
    publ.addEngine(scriptInclude)

    publ
  }

  private class WebScalaScriptEngine(name: Symbol, e: PubletEngine) extends ScalaScriptEvalEngine(name, e) {
    override def importPackages = super.importPackages ++ List(
    "org.eknet.publet.web.WebContext",
    "org.eknet.publet.web.AttributeMap",
    "org.eknet.publet.web.Key"
    )
  }
}
