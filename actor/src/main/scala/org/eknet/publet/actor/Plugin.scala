package org.eknet.publet.actor

import akka.actor.{ActorSystem, ActorRef}
import scala.concurrent.Future

/**
 * A plugin is loaded (i.e. `load` is invoked) when publet is started. A plugin
 * can depend on other plugins by naming them in `dependsOn`. The dependent
 * plugins are then loaded before this.
 *
 * Usually plugins are implemented as singleton objects. So other plugins
 * can specify them as dependents by invoking `name` on them.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.13 21:12
 */
trait Plugin {

  /**
   * The (unique) name of this plugin.
   * @return
   */
  def name: String

  /**
   * A set of plugin names this plugin depends on.
   * @return
   */
  def dependsOn: Set[String]

  /**
   * Implements startup of this plugin. The `ref` is the [[akka.actor.ActorRef]]
   * to the publet actor.
   *
   * @param ref
   * @param system
   * @return
   */
  def load(ref: ActorRef, system: ActorSystem): Future[_]

}
