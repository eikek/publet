package org.eknet.publet.actor

import akka.actor.ActorRef
import org.eknet.publet.content._
import java.net.URI
import scala.util.Try
import akka.actor.Status.{Success, Failure, Status}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.05.13 19:28
 */
object messages {

  /**
   * Event published when all plugins have been loaded.
   *
   * @param publetRef
   */
  case class Available(publetRef: ActorRef)

  case class FindContent(path: Path, params: Map[String, String] = Map.empty)

  sealed trait EngineRegistryMessage
  case class Register(ref: ActorRef, pattern: Set[Path]) extends EngineRegistryMessage
  case class Unregister(pattern: Set[Path]) extends EngineRegistryMessage
  case class GetEngine(path: Path) extends EngineRegistryMessage
  case class Conversion(req: FindContent, sources: List[Content], target: ContentType) extends EngineRegistryMessage

  // others
  case class PubletResponse(req: FindContent, resp: Try[Option[Resource]], duration: Long)
  case class FolderCreated(path: Path, folder: Folder, info: ModifyInfo)
  case class ContentCreated(path: Path, content: Content, info: ModifyInfo)
  case class ResourceDeleted(path: Path, info: ModifyInfo)

}
