package org.eknet.publet.scalate

import akka.actor.{ActorSystem, ActorRef, Props}
import org.eknet.publet.actor.{Publet, Plugin}
import akka.util.Timeout
import scala.concurrent.Future
import org.eknet.publet.content.{Path, uri}
import org.eknet.publet.actor.messages._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.05.13 22:59
 */
object ScalatePlugin extends Plugin {

  import akka.pattern.ask
  import concurrent.duration._
  import collection.JavaConverters._
  implicit val timeout: Timeout = 5.seconds

  private val partitionUri = uri("classpath://org/eknet/publet/scalate/views")

  def name = "scalate"
  def dependsOn = Set()

  def load(ref: ActorRef, system: ActorSystem) = {
    val engineActor = system.actorOf(Props(new ScalateEngine(ref)), name = "scalate-engine")
    val publet = Publet(system)
    publet.documentRoot.mount("/publet/scalate", publet.partitionFactory(partitionUri))
    val patterns = publet.settings.config.getStringList("publet.scalate-engine.pattern").asScala
    if (patterns.isEmpty) {
      Future.successful("ok")
    } else {
      val paths = patterns.toSet[String].map(s => Path(s))
      ref ? Register(engineActor, paths)
    }
  }
}
