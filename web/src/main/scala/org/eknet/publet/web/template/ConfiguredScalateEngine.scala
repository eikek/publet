package org.eknet.publet.web.template

import org.fusesource.scalate.TemplateEngine
import org.eknet.publet.Publet
import org.eknet.publet.engine.scalate.{VfsResourceLoader, ScalateEngine}
import scalate.Boot

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.05.12 23:47
 */
class ConfiguredScalateEngine(name: Symbol, publet: Publet) extends ScalateEngine(name, ConfiguredScalateEngine.createEngine()) {

  VfsResourceLoader.install(engine, publet)

  override def setDefaultLayoutUri(uri: String) {
    engine.layoutStrategy = new LayoutLookupStrategy(engine, uri)
  }

}

object ConfiguredScalateEngine {

  private def createEngine() = {
    val engine = new TemplateEngine()
    new Boot(engine).run()
    engine
  }
}