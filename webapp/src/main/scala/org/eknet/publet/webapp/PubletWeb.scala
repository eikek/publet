package org.eknet.publet.webapp

import akka.actor._
import org.eknet.publet.actor.Publet
import java.util.concurrent.atomic.AtomicReference
import com.typesafe.config.ConfigFactory

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 13.05.13 20:18
 */
object PubletWeb extends ExtensionId[PubletWebExt] with ExtensionIdProvider {

  def lookup() = PubletWeb

  def createExtension(system: ExtendedActorSystem) = {
    new PubletWebExt(system)
  }
}

class PubletWebExt(system: ExtendedActorSystem) extends Extension {

  val webSettings = new PubletWebSettings(system)

  private[webapp] val appSettingsRef = new AtomicReference(new ApplicationSettings(ConfigFactory.empty()))

  def appSettings = appSettingsRef.get()
}