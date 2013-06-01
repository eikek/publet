package org.eknet.publet.webapp

import akka.actor._
import org.eknet.publet.actor.Logging
import org.eknet.publet.actor.messages.{ContentCreated, Available}
import akka.actor.Terminated
import spray.routing.HttpServiceActor
import spray.http.HttpRequest
import com.typesafe.config.ConfigFactory

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.04.13 22:15
 */
class PubletWebActor extends Actor with HttpServiceActor with Logging  {

  val settings = PubletWeb(context.system).webSettings
  context.system.eventStream.subscribe(self, classOf[Available])

  private val maintRef = context.actorOf(Props(new Actor {
    def receive = {
      case ContentCreated(path, c, _) if (path.absoluteString == "/_config/settings.conf") => {
        val reader = io.Source.fromInputStream(c.inputStream).bufferedReader()
        val cfg = ConfigFactory.parseReader(reader)
        val appsettings = new ApplicationSettings(cfg)
        PubletWeb(context.system).appSettingsRef.set(appsettings)
        context.system.eventStream.publish(SettingsReload(appsettings))
      }
    }
    override def preStart() {
      super.preStart()
      context.system.eventStream.subscribe(self, classOf[ContentCreated])
    }
  }))

  private val loader = () => (new ExtensionOven(settings.tempdir.resolve("webappclass")))
    .bakeExtensionActor(context, context.system).get

  private val extensions = context.actorOf(Props(loader), "extensions")

  def receive = awaitAvailable

  def ready: Receive = {
    case req: HttpRequest => extensions.forward(req)
    case Terminated(ref) => {
      context.parent ! PoisonPill
    }
  }

  def awaitAvailable: Receive = {
    case Available(_) => {
      context.become(ready)
    }
    case Terminated(ref) => {
      context.parent ! PoisonPill
    }
  }

}