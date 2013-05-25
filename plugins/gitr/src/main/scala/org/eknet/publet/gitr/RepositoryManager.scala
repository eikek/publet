package org.eknet.publet.gitr

import akka.actor.{Props, Actor}
import org.eknet.publet.actor.{Publet, Logging}
import java.nio.file.Paths
import org.eknet.publet.gitr.RepositoryManager._
import org.eknet.publet.gitr.RepositoryManager.GetRepo
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}
import java.net.URI

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.13 01:01
 */
class RepositoryManager extends Actor with Logging {

  private val root = Paths.get(Publet(context.system).settings.config.getString("publet.gitr.root"))
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
  }

}

object RepositoryManager {

  final case class GetOrCreateRepo(name: RepoName, bare: Boolean)
  final case class GetRepo(name: RepoName)
  final case class GetOrCreateTandem(name: RepoName)
  final case class GetPartition(uri: URI)
}