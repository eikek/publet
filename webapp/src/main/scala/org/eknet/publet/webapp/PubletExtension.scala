package org.eknet.publet.webapp

import akka.actor.{ActorRef, Props}
import org.eknet.publet.actor.{Publet, PubletActor}
import spray.http._
import org.eknet.publet.content.{Folder, EmptyPath, Path, Content}
import spray.http.HttpResponse
import org.eknet.publet.actor.messages.FindContent
import java.io.{PrintWriter, StringWriter}
import com.typesafe.scalalogging.slf4j.Logging
import akka.util.Timeout

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 12.05.13 23:16
 */
trait PubletExtension extends WebExtension with Logging {

  val publetRef = webapp.actorOf(Props[PubletActor], "publet")

  withRoute {
    pass {
      extract(_.request) { req =>
        complete { publetRequest(req, publetRef) }
      }
    }
  }

  private def publetRequest(http: HttpRequest, publet: ActorRef) = {
    import akka.pattern.ask
    import concurrent.duration._
    implicit val timeout: Timeout = 30.seconds

    val req = FindContent(http.parseUri.path, http.parseQuery.queryParams)
    val content = ask(publet, req).mapTo[Option[Content]].map(makeResponse)
    content.recover({
      case e => {
        logger.error(">>> Error processing request!", e)
        HttpResponse(status = StatusCodes.InternalServerError, entity = HttpEntity(formatStacktrace(e)))
      }
    })
  }

  private def formatStacktrace(e: Throwable) = {
    val w = new StringWriter()
    Option(e.getCause).getOrElse(e)
      .printStackTrace(new PrintWriter(w))
    w.toString
  }
}
