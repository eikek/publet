package org.eknet.publet.actor.docroot

import akka.actor.{ActorRef, Actor}
import org.eknet.publet.content._
import org.eknet.publet.actor.messages._
import scala.concurrent.Future
import org.eknet.publet.actor.{utils, Publet, Logging, ActorRefRegistry}
import akka.util.Timeout
import org.eknet.publet.content.Resource.EmptyContent
import akka.event.EventStream
import scala.util.Success

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 08.05.13 21:43
 */
class DocumentRoot(root: Partition, map: Map[Path, List[Partition]], bus: EventStream) extends ContentTree(root, map) {

  override def mount(path: Path, partition: Partition) = {
    val next = addObject(path, partition)
    new DocumentRoot(root, next, bus)
  }

  override def unmount(path: Path) = {
    val next = removeKey(path)
    new DocumentRoot(root, next, bus)
  }

  override def createFolder(path: Path, info: ModifyInfo) = {
    val folder = super.createFolder(path, info)
    folder match {
      case Success(f) => bus.publish(FolderCreated(path, f, info))
      case _ =>
    }
    folder
  }

  override def createContent(path: Path, content: Content, info: ModifyInfo) = {
    val cont = super.createContent(path, content, info)
    cont match {
      case Success(c) => bus.publish(ContentCreated(path, c, info))
      case _ =>
    }
    cont
  }

  override def delete(path: Path, info: ModifyInfo) = {
    val del = super.delete(path, info)
    del match {
      case Success(true) => bus.publish(ResourceDeleted(path, info))
      case _ =>
    }
    del
  }
}
