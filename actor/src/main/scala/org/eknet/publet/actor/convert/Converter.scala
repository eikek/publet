package org.eknet.publet.actor.convert

import akka.actor.Actor
import org.eknet.publet.actor.{messages, ActorRefRegistry, Logging}
import org.eknet.publet.content.{Path, Glob}
import messages._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 08.05.13 23:12
 */
class Converter extends ActorRefRegistry[Glob] with Actor with Logging {

  def receive: Receive = forwarding orElse manage

  def forwarding: Receive = {
    case Register(ref, pattern) => {
      log.info(s"Registering engine ${pattern.map(_.toString).mkString(", ")} -> $ref")
      manage(AddRef(ref, pattern.map(p => Glob(p.toString)).toSeq))
    }
    case Unregister(pattern) => {
      log.info(s"Unregistering engine at ${pattern.map(_.toString).mkString(", ")}")
      manage(RemoveRef(pattern.map(p => Glob(p.toString)).toSeq))
    }
    case GetEngine(p) => sender ! find(p)
    case req @ Conversion(findReq, s, t) => {
      log.debug(s">>> Conversion requested for '${s.map(_.name.fullName).mkString(", ")}' -> $t")
      val response = req.sources match {
        case Nil => None
        case list => list.map(s => find(findReq.path.sibling(s.name))).find(_.isDefined).map(_.get)
      }
      log.debug(s">>> Returning engine actor '${response.map(_.path.name)}'")
      response.map { _.forward(req) } getOrElse {
        sender ! None
      }
    }
  }

  private[actor] def find(path: Path) = findName(path).flatMap(_.headOption)

  private[actor] def findName(path: Path) = {
    val list = keys.toList.sortBy(- _.pattern.length)
    list.find(g => g.matches(path.toString))
      .flatMap(k => get(k))
  }
}
