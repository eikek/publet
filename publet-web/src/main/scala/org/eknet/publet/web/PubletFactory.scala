package org.eknet.publet.web

import org.eknet.publet.engine._
import org.eknet.publet.resource.ContentType._
import org.eknet.publet.Publet
import template._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 23:15
 */
object PubletFactory {

  def createPublet(): Publet = {
    val publ = Publet()

    publ.register("/*", new DefaultEngine(publ))

    val editEngine = new HtmlTemplateEngine('edit, EditEngine) with FilebrowserTemplate
    publ.addEngine(editEngine)

    val listEngine = new ListEngine(publ)
    publ.addEngine(listEngine)
    publ
  }

}
