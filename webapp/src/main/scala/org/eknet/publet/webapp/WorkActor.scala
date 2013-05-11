package org.eknet.publet.webapp

import akka.actor._
import spray.http._
import org.eknet.publet.actor.{patterns, Publet, Logging, PubletActor}
import org.eknet.publet.content._
import akka.pattern.ask
import java.io.{PrintWriter, StringWriter, InputStream}
import akka.util.Timeout
import scala.concurrent.duration._
import spray.http.ContentType
import spray.io.IOServer.Bind
import org.eknet.publet.actor.messages.{FindContent, Available}
import spray.http.HttpResponse
import akka.actor.Terminated
import org.eknet.publet.webapp.WorkActor.Ready
import spray.can.server.HttpServer
import spray.http.HttpHeaders.`Last-Modified`

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.04.13 22:15
 */
class WorkActor extends Actor with Logging {
  import context.dispatcher
  import akka.pattern.pipe
  implicit private val timeout: Timeout = 30.seconds

  val publet = context.watch(context.actorOf(Props[PubletActor], "publet"))
  val settings = Publet(context.system).settings

  val bindAddress = settings.config.getString("publet.server.bindAddress")
  val port = settings.config.getInt("publet.server.port")

  context.system.eventStream.subscribe(self, classOf[Available])

  private[this] var server: ActorRef = null
  private[this] var available = false

  def receive = waitForReady


  def ready: Receive = {
    case http: HttpRequest => {
      val stopTime = patterns.Stopwatch.start()
      val req = FindContent(http.parseUri.path, http.parseQuery.queryParams)
      val content = ask(publet, req).mapTo[Option[Content]]
        .map(or => or.map(c => {

        val body = HttpBody(mimeType(c), streamToArray(c.inputStream))
        val date = c.lastModification.map(DateTime.apply).getOrElse(DateTime.now)
        HttpResponse(entity = body, headers = List(`Last-Modified`(date)))
      }))
        .map(or => or.getOrElse(HttpResponse(status = StatusCodes.NotFound)))

      content.onComplete {
        case x => {
          log.debug(s"~~~~~~~~~~ http response time for '${req.path.absoluteString}' was ${stopTime()} ms")
        }
      }
      content.recover({
        case e => {
          log.error(e, ">>> Error processing request!")
          HttpResponse(status = StatusCodes.InternalServerError, entity = HttpEntity(WorkActor.formatStacktrace(e)))
        }
      }) pipeTo sender
    }
    case Terminated(ref) => {
      context.parent ! PoisonPill
    }
  }

  def waitForReady: Receive = awaitAvailable orElse {
    case Ready(ref) => {
      this.server = ref
      if (this.available) {
        server ! HttpServer.Bind(bindAddress, port)
        context.become(ready)
      } else {
        context.become(awaitAvailable)
      }
    }
  }

  def awaitAvailable: Receive = {
    case Available(_) => {
      this.available = true
      if (server != null) {
        server ! Bind(interface = bindAddress, port = port)
        context.become(ready)
      }
    }
    case Terminated(ref) => {
      context.parent ! PoisonPill
    }
  }

  def streamToArray(in: InputStream) = Stream.continually(in.read).takeWhile(_ != -1).toArray.map(_.toByte)

  def mimeType(r: Resource) = MediaTypes.forExtension(r.name.ext).map(mt => ContentType(mt)).getOrElse(ContentType.`text/plain`)
}

object WorkActor {
  def formatStacktrace(e: Throwable) = {
    val w = new StringWriter()
    Option(e.getCause).getOrElse(e)
      .printStackTrace(new PrintWriter(w))
    w.toString
  }

  case class Ready(server: ActorRef)
}