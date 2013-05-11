package org.eknet.publet.actor.docroot

import akka.actor.{Props, Actor}
import org.eknet.publet.actor.{ActorRefRegistry, Publet, Logging}
import java.net.URI
import org.eknet.publet.actor.messages.{GetPartition, UninstallFactory, InstallFactory}
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.13 18:48
 */
class PartitionFactoryActor extends Actor with ActorRefRegistry[String] with Logging {

  private val publet = Publet(context.system)

  private val factory = publet.partitionFactory

  private def isAvailable(uri: URI) = factory.getFactory(uri.getScheme).isDefined

  private val partCount = new AtomicInteger(0)

  def localGet: Receive = {
    case GetPartition(uri) => {
      if (isAvailable(uri)) {
        sender ! Some(context.actorOf(Props(new PartitionActor(factory(uri))), name = uri.getScheme+partCount.getAndIncrement))
      } else {
        sender ! None
      }
    }
  }

  def resolvedGet: Receive = {
    case m @ GetPartition(uri) if (resolve(uri).isDefined)  => {
      resolve(uri).get.forward(m)
    }
  }

  def install: Receive = manage orElse {
    case InstallFactory(ref, schemes) => {
      log.debug(s"Install partition factory for schemes '${schemes.mkString(", ")}'")
      manage(AddRef(ref, schemes.toSeq))
    }
    case UninstallFactory(schemes) => {
      log.debug(s"Remove partition factory for schemes '${schemes.mkString(", ")}'")
      manage(RemoveRef(schemes.toSeq))
    }
  }

  def receive: Receive = resolvedGet orElse localGet orElse install

  private def resolve(uri: URI) = {
    keys.find(s => s == uri.getScheme).flatMap(s => get(s)).flatMap(_.headOption)
  }
}