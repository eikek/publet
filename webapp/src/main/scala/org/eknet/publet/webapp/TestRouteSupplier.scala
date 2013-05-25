package org.eknet.publet.webapp

import spray.routing.Directives
import akka.actor.{ActorSystem, ActorRef, ActorRefFactory}
import scala.concurrent.Future
import org.eknet.publet.webapp.extensions.WebExtension

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 12.05.13 23:09
 */
trait TestRouteSupplier extends Directives {
  self: WebExtension =>

  withRoute {
    path("git" / PathElement) { el =>
      complete("Some answer: "+ el)
    }
  }

}
