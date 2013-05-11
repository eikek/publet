package org.eknet.publet.webapp

import akka.actor.Actor
import org.eknet.publet.content.Source.StringSource
import org.eknet.publet.content.ContentType
import org.eknet.publet.actor.docroot.ActorResource.Params
import org.eknet.publet.actor.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.13 13:26
 */
class TestActorResource extends Actor with Logging {

  def receive = {
    case Params(name, p) => {
      val name = p.get("name").getOrElse("foreigner")
      val c = StringSource(s"# Hello $name", ContentType.`text/x-markdown`)
      sender ! Some(c)
    }
  }
}