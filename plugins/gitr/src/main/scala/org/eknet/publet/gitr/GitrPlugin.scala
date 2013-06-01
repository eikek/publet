package org.eknet.publet.gitr

import akka.actor.{ActorSystem, ActorRef, Props}
import org.eknet.publet.actor.{Publet, Plugin}
import akka.util.Timeout
import org.eknet.publet.content.{Partition, PartitionSupplier}
import org.eknet.publet.gitr.RepositoryManager.GetPartition
import scala.concurrent.Await

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.13 01:00
 */
object GitrPlugin extends Plugin {

  import akka.pattern.ask
  import concurrent.duration._

  def name = "gitr"

  def dependsOn = Set()

  def load(ref: ActorRef, system: ActorSystem) = {
    implicit val timeout: Timeout = 7.seconds
    val repoMan = GitrExtension(system).repoMan
    val gitFactory: PartitionSupplier = uri => {
      val f = (repoMan ? GetPartition(uri)).mapTo[Partition]
      Await.result(f, Duration(5, SECONDS))
    }
    Publet(system).partitionFactory.alter(f => f.update("git", gitFactory))
  }
}
