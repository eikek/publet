package org.eknet.publet.actor

import akka.actor.{Actor, ActorRef}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.05.13 00:25
 */
trait Logging {
  this: Actor =>
  val log = akka.event.Logging(context.system.eventStream, createName)

  private def createName = loggerName match {
    case c: Class[_] => c.getName
    case ref: ActorRef => pathToLoggername(ref)
    case actor: Actor => pathToLoggername(actor.self)
    case o => o.toString
  }

  private def pathToLoggername(ref: ActorRef) = {
    ref.path.elements.map(s => s.replace("$", "")).mkString(".")
  }

  def loggerName: AnyRef = this
}
