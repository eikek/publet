package org.eknet.publet.webapp.extensions

import spray.routing._
import akka.actor.{ActorRefFactory, ActorSystem, Actor}
import org.eknet.publet.webapp.assets.AssetExtension
import org.eknet.publet.webapp.RequestCycle
import org.eknet.publet.actor.utils
import org.eknet.publet.webapp.scalate.ScalateInit

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 14.05.13 19:07
 */
private[extensions] class WebExtensionImpl(val webapp: ActorRefFactory, val system: ActorSystem) extends Collector
  with WebExtension with Actor with ScalateInit with HttpServiceActor with AssetExtension {

  def receive = {
    val route = myroute(routes.reduce(_ ~ _))
    runRoute(route)
  }

  def myroute(delegate: Route): Route = { ctx =>
    val stop = utils.Stopwatch.start()
    delegate(ctx.mapHttpResponse(r => {
      context.system.eventStream.publish(RequestCycle(ctx.request, r, stop()))
      r
    }))
  }
}

private[extensions] abstract class Collector {

  private[this] val proutes = collection.mutable.ListBuffer[Route]()

  final def withRoute(route: Route) {
    this.proutes.append(route)
  }
  final protected def routes = proutes.toList
}