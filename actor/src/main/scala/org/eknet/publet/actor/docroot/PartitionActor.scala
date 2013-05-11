package org.eknet.publet.actor.docroot

import akka.actor.{Props, Actor}
import org.eknet.publet.content.{Folder, Partition}
import scala.Some
import org.eknet.publet.actor.Logging
import org.eknet.publet.actor.messages._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.05.13 20:43
 */
class PartitionActor(partition: Partition) extends Actor with Logging {

  def receive = {
    case Find(path) => {
      val r = partition.find(path)
      log.debug(s">> Found resource ${r.map(_.name)} for path ${path.absoluteString}")
      sender ! r
    }
    case Select(path) => sender ! partition.select(path)
    case Listing(path) => {
      val result = partition.find(path) match {
        case Some(f: Folder) => f.children
        case _ => Nil
      }
      sender ! result
    }
    case GetResourceType(path) => sender ! partition.getResourceType(path)
    case CreateFolder(path, info) => sender ! partition.createFolder(path, info)
    case CreateContent(path, content, info) => sender ! partition.createContent(path, content, info)
    case Delete(path, info) => sender ! partition.delete(path, info)
  }
}

object PartitionActor {

  def apply(factory: Partition): Props = Props(new PartitionActor(factory))
}