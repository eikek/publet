package org.eknet.publet.webapp

import org.eknet.publet.actor.{Publet, Plugin}
import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import org.eknet.publet.content.SimplePartition
import org.eknet.publet.webapp.assets.AssetLoader

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 22.05.13 10:19
 *
 */
object WebappPlugin extends Plugin {

  def name = "webapp"

  def dependsOn = Set()

  def load(ref: ActorRef, system: ActorSystem) = {
    val publet = Publet(system)
    implicit val timeout = Timeout(4000)

    val templates = publet.createPartition("classpath:///org/eknet/publet/webapp/includes/templates")
    val container = new SimplePartition(Seq(
      new AssetLoader(PubletWeb(system).webSettings)
    ))

    publet.documentRoot alter { dr =>
      dr.mount("/publet/webapp/includes", container).mount("/publet/webapp/templates", templates)
    }
  }
}
