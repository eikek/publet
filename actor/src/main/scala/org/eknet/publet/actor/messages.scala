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

  sealed trait DocumentRootMessage
  final case class Mount(ref: ActorRef, paths: Set[Path]) extends DocumentRootMessage
  final case class MountUri(uri: URI, paths: Set[Path]) extends DocumentRootMessage
  final case class Unmount(paths: Set[Path]) extends DocumentRootMessage

  case class Find(path: Path) extends DocumentRootMessage
  case class Select(path: Path) extends DocumentRootMessage
  case class Listing(path: Path) extends DocumentRootMessage
  case class GetResourceType(path: Path) extends DocumentRootMessage
  case class CreateFolder(path: Path, info: ModifyInfo) extends DocumentRootMessage
  case class CreateContent(path: Path, content: Content, info: ModifyInfo) extends DocumentRootMessage
  case class Delete(path: Path, info: ModifyInfo) extends DocumentRootMessage

  sealed trait PartitionFactoryMessage
  final case class GetPartition(uri: URI) extends PartitionFactoryMessage
  final case class InstallFactory(ref: ActorRef, schemes: Set[String]) extends PartitionFactoryMessage
  final case class UninstallFactory(schemes: Set[String]) extends PartitionFactoryMessage

  sealed trait EngineRegistryMessage
  case class Register(ref: ActorRef, pattern: Set[Path]) extends EngineRegistryMessage
  case class Unregister(pattern: Set[Path]) extends EngineRegistryMessage
  case class GetEngine(path: Path) extends EngineRegistryMessage
  case class Conversion(path: Path, sources: List[Content], target: ContentType) extends EngineRegistryMessage

  // others
  case class PubletResponse(req: FindContent, resp: Try[Option[Resource]], duration: Long)

}
