package org.eknet.publet.scalate

import akka.actor.{ActorRef, Actor}
import org.fusesource.scalate.TemplateEngine
import org.eknet.publet.content._
import org.fusesource.scalate.util.ResourceLoader
import scala.concurrent.{Future, Await}
import org.eknet.publet.content.Source.StringSource
import org.eknet.publet.actor.{Logging, Publet}
import scala.util.Try
import java.io.{ByteArrayInputStream, StringWriter, PrintWriter}
import org.eknet.publet.actor.messages._
import java.nio.file.{StandardCopyOption, Files}
import org.fusesource.scalate.support.ScalaCompiler
import scala.tools.nsc.interpreter.AbstractFileClassLoader
import scala.reflect.io.AbstractFile

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.05.13 21:53
 */
class ScalateEngine(ce: ConfiguredEngine, ref: ActorRef) extends Actor with Logging {
  private val engine = ce.engine

  import akka.pattern.pipe
  import context.dispatcher

  private def toSource(source: Content, str: String):Source = new StringSource(str, ContentType.`text/html`, source.lastModification)

  def receive = {
    case Conversion(FindContent(path, _), sources, ContentType(_, "html", false)) => {
      Future {
        val source = sources.find(s => engine.extensions.contains(s.name.ext))
        val result = Try {
          source.map(c => new ContentTemplate(path.parent / c.name, c))
            .map(c => (c, engine.layout(c, ce.attributes)))
            .map(t => toSource(t._1.content, t._2))
        } recover {
          case e: Exception => {
            log.error(e, "Error processing template!")
            Some(StringSource(engine.layout("/publet/scalate/_error.jade",
              ce.attributes.updated("ex", formatStacktrace(e)))))
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
