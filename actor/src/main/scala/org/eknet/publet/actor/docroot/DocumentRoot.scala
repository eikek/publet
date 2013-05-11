package org.eknet.publet.actor.docroot

import akka.actor.{ActorRef, Actor}
import org.eknet.publet.content.{Folder, Path}
import org.eknet.publet.actor.messages._
import scala.concurrent.Future
import org.eknet.publet.actor.{patterns, Publet, Logging, ActorRefRegistry}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 08.05.13 21:43
 */
class DocumentRoot extends ActorRefRegistry[Path] with Actor with Logging {

  import context.dispatcher
  import akka.pattern.ask
  import akka.pattern.pipe
  import patterns.defaultTimeout

  private val docRoot = Publet(context.system).documentRoot

  def receive: Receive = forwarding orElse manage

  def forwarding: Receive = {
    case Mount(ref, paths) => {
      log.info(s"Mounting actor '${ref.path.name}' to '${paths.map(_.absoluteString).mkString(", ")}' ...")
      manage(AddRef(ref, paths.toSeq))
    }
    case MountUri(uri, paths) => {
      val f = (context.parent ? GetPartition(uri)).mapTo[Option[ActorRef]].flatMap(op => op match {
        case Some(actor) => (self ? Mount(actor, paths))
        case None => Future.successful(None)
      })
      f pipeTo sender
    }
    case Unmount(paths) => manage(RemoveRef(paths.toSeq))
    case Find(path) => {
      resolve(path).forward(Find.apply) getOrElse {
        sender ! docRoot.find(path)
      }
    }
    case Select(path) => {
      resolve(path).forward(Select.apply) getOrElse {
        sender ! docRoot.select(path)
      }
    }
    case Listing(path) => {
      resolve(path).forward(Listing.apply) getOrElse {
        sender ! (docRoot.find(path) match {
          case Some(f: Folder) => f.children
          case _ => Nil
        })
      }
    }
    case GetResourceType(path) => {
      resolve(path).forward(GetResourceType.apply) getOrElse {
        sender ! docRoot.getResourceType(path)
      }
    }
    case CreateFolder(path, info) => {
      resolve(path).forward(p => CreateFolder(p, info)) getOrElse {
        sender ! docRoot.createFolder(path, info)
      }
    }
    case CreateContent(path, c, info) => {
      resolve(path).forward(p => CreateContent(p, c, info)) getOrElse {
        sender ! docRoot.createContent(path, c, info)
      }
    }
    case Delete(path, info) => {
      resolve(path).forward(p => Delete(p, info)) getOrElse {
        sender ! docRoot.delete(path, info)
      }
    }
  }

  private[actor] def resolve(path: Path) = {
    val pair = keys.toList.sortBy(- _.size) //sort mounted paths by size desc
      .find(p => p.isEmpty || path.startsWith(p))  //find first with matching path
      .flatMap(p => get(p).map(list => (path.drop(p.size), list.head)))
    Resolved(pair)
  }

  private[actor] case class Resolved(t: Option[(Path, ActorRef)]) {
    def forward[A](f: Path => A) = t.map(r => r._2.forward(f(r._1)))
  }
}
