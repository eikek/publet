package org.eknet.publet.web

import org.eknet.publet.engine._
import org.eknet.publet.resource.ContentType._
import org.eknet.publet.Publet
import scalascript.ScalaScriptEvalEngine
import template._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 23:15
 */
object PubletFactory {

  def createPublet(): Publet = {
    val publ = Publet()

    val defaultEngine = new DefaultEngine(publ)
    publ.register("/*", defaultEngine)

    val editEngine = new HtmlTemplateEngine('edit, EditEngine) with FilebrowserTemplate
    publ.addEngine(editEngine)

    val scalaEngine = new ScalaScriptEvalEngine('eval, defaultEngine)
    publ.addEngine(scalaEngine)
    publ
  }

}
