package org.eknet.publet.webapp

import com.typesafe.config.ConfigFactory
import akka.actor.ExtendedActorSystem
import org.eknet.publet.actor.Publet
import org.eknet.publet.content.Path

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 13.05.13 20:17
 */
class PubletWebSettings(system: ExtendedActorSystem) {
  private val publetSettings = Publet(system).settings
  val config = {
    val cfg = system.settings.config
    cfg.checkValid(ConfigFactory.defaultReference(), "publet.publetweb")
    cfg
  }

  import collection.JavaConverters._
  import config._

  val extensions = getStringList("publet.publetweb.extensions").asScala.toList

  val runMode = getString("publet.publetweb.run-mode")
  val isProduction = runMode.toLowerCase == "production"
  val isDevelopment = !isProduction


  val tempdir = publetSettings.tempdir
  val workdir = publetSettings.workdir

  val urlBase = getString("publet.publetweb.url-base")

  def urlFor(path: String) = {
    val buf = new StringBuilder(urlBase)
    if (!urlBase.endsWith("/"))
      buf.append("/")
    if (path.startsWith("/")) {
      buf.append(path.substring(1))
    } else {
      buf.append(path)
    }
    buf.toString()
  }

  val assetsBasePath = Path(getString("publet.publetweb.assets-base-path "))
}
