package org.eknet.publet.webapp

import akka.actor.Actor
import org.eknet.publet.actor.Logging
import org.eknet.publet.actor.messages.{PubletResponse, Available}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.05.13 19:36
 */
class TestActor extends Actor with Logging {

  context.system.eventStream.subscribe(self, classOf[Available])
  context.system.eventStream.subscribe(self, classOf[PubletResponse])

  def receive = {
    case Available(ref) => {
      log.info("+++++ publet available ++++++")
    }
    case PubletResponse(req, res, duration) => {
      log.debug(s"~~~~~~~~~~~~~~~ publet '${req.path.absoluteString}' takes $duration ms ~~~~~~~~~~~~~~~~~~~")
    }
  }

}
