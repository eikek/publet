package org.eknet.publet.dist

import akka.kernel.Bootable
import com.typesafe.config.{Config, ConfigFactory}
import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import org.eknet.publet.webapp.PubletWebActor
import org.eknet.publet.actor.messages.Available
import akka.util.Timeout
import spray.can.server.{SprayCanHttpServerApp, HttpServer}
import scala.util.{Failure, Success}
import com.typesafe.scalalogging.slf4j.Logging
import org.eknet.publet.actor

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 13.05.13 17:20
 */
class Boot extends Bootable with SprayCanHttpServerApp with Logging {
  val config = ConfigFactory.load()

  override lazy val system = ActorSystem("publet-server", config)

  def startup() {
    val handler = system.actorOf(Props[PubletWebActor], name = "publet-http")
    val server = newHttpServer(handler, name = "spray-http-server")
    system.actorOf(Props(new StartupActor(server, config)))
  }

  def shutdown() {
    system.shutdown()
  }
}

class StartupActor(server: ActorRef, config: Config) extends Actor with actor.Logging {
  context.system.eventStream.subscribe(self, classOf[Available])

  val bindAddress = config.getString("publet.server.bindAddress")
  val port = config.getInt("publet.server.port")

  import akka.pattern.ask
  import context.dispatcher
  import scala.concurrent.duration._
  implicit val timeout: Timeout = 5.seconds

  def receive = {
    case Available(ref) => {
      val f = server ? HttpServer.Bind(bindAddress, port)
      f.onComplete {
        case Success(_) => log.info("======== Server ready. =========")
        case Failure(e) => {
          log.error("Error starting server!", e)
          System.exit(1)
        }
      }
      context.stop(self)
    }
  }
}
