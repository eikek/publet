package org.eknet.publet.actor.docroot

import org.eknet.publet.content.{Source, DynamicContent, Name}
import akka.actor.ActorRef
import scala.concurrent.{Future, ExecutionContext}
import akka.util.Timeout
import org.eknet.publet.content.Resource.SimpleContent

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.13 13:36
 */
object ActorResource {

  private[actor] final class DynActorResource(val name: Name, val ref: ActorRef) extends DynamicContent {
    import akka.pattern.ask

    def create(params: Map[String, String]) = sys.error("not implemented")

    def eval(p: Params)(implicit ec: ExecutionContext, timeout: Timeout) =
      (ref ? p).mapTo[Option[Source]].map(s => s.map(SimpleContent(name, _)))

    override def toString = s"${getClass.getSimpleName}[name=$name, ref=$ref]"
  }

  final case class Params(name: Name, p: Map[String, String])

  /**
   * Wraps an `ActorRef` and a `Name` into a `DynamicContent` resource. The
   * returned resource may not be "created" via its `create` method. Instead
   * publet is sending the `Params` message to the wrapped actor.
   *
   * @param name
   * @param ref
   * @return
   */
  def apply(name: Name, ref: ActorRef) : DynamicContent = new DynActorResource(name, ref)

}
