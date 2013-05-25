package org.eknet.publet.scalate

import akka.actor._
import org.eknet.publet.content.Registry
import org.fusesource.scalate.TemplateEngine
import org.eknet.publet.actor.{Publet, PubletSettings}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 12.05.13 13:44
 */
object PubletScalate extends ExtensionId[PubletScalateExt] with ExtensionIdProvider {

  def lookup() = PubletScalate

  def createExtension(system: ExtendedActorSystem) = {
    new PubletScalateExt(system)
  }
}

class PubletScalateExt(system: ActorSystem) extends Extension {

  val publetSettings = Publet(system).settings
  val scalateSettings = new ScalateSettings(publetSettings.config)

  private object Initializers extends Registry {
    type Key = String
    type Value = ConfiguredEngine => Unit

    def getFor(name: String) = super.get(name).getOrElse(Nil).reverse
  }

  /**
   * Adds a function to a list that is applied when a [[org.fusesource.scalate.TemplateEngine]]
   * is first created.
   *
   * @param name
   * @param init
   */
  def addInitializer(name: String, init: ConfiguredEngine => Unit) {
    Initializers.register(name, init)
  }

  private[scalate] def initialize(name: String, ce: ConfiguredEngine) {
    val wordkir = publetSettings.workdir.resolve("scalate").resolve(name)
    ce.engine.workingDirectory = wordkir.toFile

    val engineCfg = scalateSettings.engineConfigs(name)
    engineCfg.setTo(ce.engine)

    //apply settings from other plugins
    Initializers.getFor(name).foreach(f => f(ce))
  }
}
