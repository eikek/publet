package org.eknet.publet.webapp

import akka.actor.{ActorRefFactory, ActorSystem}
import spray.routing.{Directives, Route}
import scala.concurrent.ExecutionContext

/**
 * Use this as self type when creating extensions. If an extensions depends
 * on others, add them as self types, too. All available extensions are finally
 * mixed into one class on startup.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 14.05.13 06:54
 *
 */
trait WebExtension extends Directives {

  def webapp: ActorRefFactory
  def system: ActorSystem
  implicit def executionContext: ExecutionContext
  implicit def actorRefFactory: ActorRefFactory

  def withRoute(route: Route)
}


