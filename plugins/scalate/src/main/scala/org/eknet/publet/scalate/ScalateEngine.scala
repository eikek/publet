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

  private def toSource(source: Content, str: String, target: ContentType):Source = new StringSource(str, target, source.lastModification)

  def receive = {
    case Conversion(FindContent(path, params), sources, target@ContentType(_, _, false)) if (!sources.isEmpty) => {
      Future {
        val attributes = params.get("layout") match {
          case Some(l) => ce.attributes.updated("params", params).updated("layout", l)
          case _ => ce.attributes.updated("params", params)
        }
        val source = sources.find(s => engine.extensions.contains(s.name.ext))
        val result = Try {
          source.map(c => new ContentTemplate(path.sibling(c.name), c))
            .map(c => (c, engine.layout(c, attributes)))
            .map(t => toSource(t._1.content, t._2, target))
        } recover {
          case e: Exception => {
            log.error(e, "Error processing template!")
            Some(StringSource(engine.layout("/publet/scalate/_error.jade",
              attributes.updated("ex", formatStacktrace(e)))))
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
