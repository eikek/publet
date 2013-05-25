package org.eknet.publet.webapp

import akka.actor.Actor
import org.eknet.publet.actor.Logging
import org.eknet.publet.actor.messages.{PubletResponse, Available}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.05.13 19:36
 */
class TestActor extends Actor with Logging {

  def receive = {
    case Available(ref) => {
      log.info("+++++ publet available ++++++")
    }
    case PubletResponse(req, res, duration) => {
      log.info(s"~~~~~~~~~~~~~~~ publet '${req.path.absoluteString}' takes $duration ms ~~~~~~~~~~~~~~~~~~~")
    }
    case RequestCycle(req, resp, time) => {
      log.info(s"~~~~~~~~~~~~~~~ http '${req.path}' takes $time ms ~~~~~~~~~~~~~~~~~~~")
    }
  }

  override def preStart() {
    context.system.eventStream.subscribe(self, classOf[Available])
    context.system.eventStream.subscribe(self, classOf[PubletResponse])
    context.system.eventStream.subscribe(self, classOf[RequestCycle])
  }
}
