package org.eknet.publet.gitr

import akka.actor.Actor
import org.eknet.publet.actor.Logging
import java.nio.file.{Path => JPath, Paths}
import org.eknet.publet.gitr.RepositoryManager._
import org.eknet.publet.gitr.RepositoryManager.GetRepo
import java.net.URI

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.13 01:01
 */
class RepositoryManager(root: JPath) extends Actor with Logging {

  private val repoMan = new GitrManager(root)

  def receive = {
    case GetPartition(uri) => {
      val name = RepoName(Paths.get("/" + uri.getSchemeSpecificPart).toString.substring(1))
      val tandem = repoMan.getTandem(name).getOrElse(repoMan.createTandem(name).get)
      val p = new GitPartition(tandem)
      log.info(s"Created git partition for repo '${tandem.name.simpleName}' on path '${p.directory}'")
      sender ! p
    }
    case GetOrCreateRepo(name, bare) => sender ! repoMan.get(name).getOrElse(repoMan.create(name, bare))
    case GetRepo(name) => sender ! repoMan.get(name)
    case GetOrCreateTandem(name) => {
      val tandem = repoMan.getTandem(name).getOrElse(repoMan.createTandem(name).get)
      sender ! tandem
    }
    case SyncTandem(name) => {
      val ref = repoMan.getTandem(name) map { t =>
        log.info("Updating worktree of tandem: "+ name)
        t.updateWorkTree()
      }
      sender ! ref
    }
  }

}

object RepositoryManager {

  final case class GetOrCreateRepo(name: RepoName, bare: Boolean)
  final case class GetRepo(name: RepoName)
  final case class GetOrCreateTandem(name: RepoName)
  final case class GetPartition(uri: URI)
  final case class SyncTandem(name: RepoName)
}