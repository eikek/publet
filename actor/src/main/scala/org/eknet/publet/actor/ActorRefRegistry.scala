package org.eknet.publet.actor

import akka.actor.{Terminated, Actor, ActorRef}
import scala.util.Success

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 08.05.13 22:04
 */
trait ActorRefRegistry[K] {
  this: Actor =>

  private val mappings = collection.mutable.Map.empty[K, List[ActorRef]]

  case class AddRef(ref: ActorRef, keys: Seq[K])
  case class RemoveRef(keys: Seq[K])

  def manage: Receive = {
    case AddRef(ref, keys) => {
      context.watch(ref)
      keys.foreach(mount(ref, _))
      sender ! Success(keys)
    }
    case RemoveRef(keys) => {
      keys.foreach(unmount)
      sender ! Success(keys)
    }
    case Terminated(ref) => {
      removeRef(ref)
    }
  }

  def keys = mappings.keys

  def get(key: K) = mappings.get(key)

  def mount(ref: ActorRef, key: K) {
    val list = mappings.get(key).getOrElse(Nil)
    mappings.put(key, ref :: list)
  }

  def unmount(key: K) {
    mappings.get(key) match {
      case Some(a::Nil) => mappings.remove(key)
      case Some(a::as) => mappings.put(key, as)
      case _ =>
    }
  }

  def removeRef(ref: ActorRef) {
    mappings.foreach(t => {
      if (t._2.contains(ref)) {
        mappings.put(t._1, (t._2.filterNot(_ == ref)))
      }
    })
  }

}
