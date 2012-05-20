package org.eknet.publet.web.template

import org.fusesource.scalate.TemplateEngine
import org.eknet.publet.Publet
import org.eknet.publet.engine.scalate.{VfsResourceLoader, ScalateEngine}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.05.12 23:47
 */
class ConfiguredScalateEngine(name: Symbol, publet: Publet) extends ScalateEngine(name, new TemplateEngine()) {

  VfsResourceLoader.install(engine, publet)

  override def setDefaultLayoutUri(uri: String) {
    engine.layoutStrategy = new LayoutLookupStrategy(engine, uri)
  }

}
