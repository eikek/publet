package org.eknet.publet.actor.convert

import akka.actor.Actor
import org.eknet.publet.content.{Content, Engine}
import scala.concurrent.Future
import org.eknet.publet.actor.{messages, Logging}
import messages.Conversion

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.05.13 16:07
 */
class EngineActor(engine: Engine) extends Actor with Logging {

  import context.dispatcher
  import akka.pattern.pipe

  def receive = {
    case Conversion(path, sources, target) => {
      log.debug(s">>> Converting '${sources.map(_.name).mkString(",")}' to $target")
      val converted = Future {
        val converter = engine.lift(_ : Content, target)
        sources.toStream.map(converter).filter(_.isDefined).headOption.flatten
      }
      converted pipeTo sender
    }
  }
}
