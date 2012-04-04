package org.eknet.publet.web

import org.eknet.publet.engine._
import org.eknet.publet.resource.ContentType._
import template.YamlEngine
import org.eknet.publet.Publet

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 23:15
 */
object PubletFactory {

  def createPublet(): Publet = {
    val publ = Publet()

    val conv = ConverterEngine()
    conv.addConverter(markdown -> html, KnockoffConverter)
    publ.register("/*", new YamlEngine('default, conv))

    val editEngine = new YamlEngine('edit, EditEngine)
    publ.addEngine(editEngine)
    publ
  }

}
