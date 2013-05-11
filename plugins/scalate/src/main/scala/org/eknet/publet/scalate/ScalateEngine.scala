package org.eknet.publet.scalate

import akka.actor.{ActorRef, Actor}
import org.fusesource.scalate.TemplateEngine
import org.eknet.publet.content._
import org.fusesource.scalate.util.ResourceLoader
import scala.concurrent.{Future, Await}
import org.eknet.publet.content.Source.StringSource
import org.eknet.publet.actor.{Logging, Publet}
import scala.util.Try
import java.io.{StringWriter, PrintWriter}
import org.eknet.publet.actor.messages._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.05.13 21:53
 */
class ScalateEngine(ref: ActorRef) extends Actor with Logging {

  import akka.pattern.pipe
  import akka.pattern.ask
  import scala.concurrent.duration._
  import org.eknet.publet.actor.patterns.defaultTimeout
  import context.dispatcher

  context.system.eventStream.subscribe(self, classOf[Available])

  private val settings = Publet(context.system).settings
  val engine = new TemplateEngine()
  engine.workingDirectory = settings.workdir.resolve("scalate").toFile
  engine.allowCaching = settings.config.getBoolean("publet.scalate-engine.allow-caching")
  engine.allowReload = true
  engine.resourceLoader = new ResourceLoader {
    def resource(uri: String) = {
      val f = (ref ? Find(uri)).mapTo[Option[Resource]]
      val res = Await.result(f, 6.seconds) //todo this is not nice, but don't know how to handle it
      //todo if the resource is dynamic, how to evaluate it here? need to pass params map
      res match {
        case Some(c: Content) => Some(new ContentTemplate(uri, c))
        case Some(r) => log.error(s"Cannot process non-content resource '$r' "); None
        case _ => None
      }
    }
  }

  private def toSource(source: Content, str: String):Source = new StringSource(str) {
    override def lastModification = source.lastModification
  }

  def receive = {
    case Conversion(path, sources, ContentType(_, "html", false)) => {
      Future {
        val source = sources.find(s => engine.extensions.contains(s.name.ext))
        val result = Try {
          source.map(c => new ContentTemplate(path.parent / c.name, c))
            .map(c => (c, engine.layout(c)))
            .map(t => toSource(t._1.content, t._2))
        } recover {
          case e: Exception => {
            log.error(e, "Error processing template!")
            Some(StringSource(engine.layout("/publet/scalate/_error.jade", Map("ex" -> formatStacktrace(e)))))
          }
        }
        result.get
      } pipeTo sender
    }
  }

  def formatStacktrace(e: Throwable) = {
    val w = new StringWriter()
    Option(e.getCause).getOrElse(e)
      .printStackTrace(new PrintWriter(w))
    w.toString
  }

}
