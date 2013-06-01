package org.eknet.publet.gitr

import akka.actor._
import java.nio.file.Paths
import org.eknet.publet.actor.Publet

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.05.13 13:05
 */
object GitrExtension extends ExtensionId[GitrExtensionImpl] with ExtensionIdProvider {
  def lookup() = GitrExtension

  def createExtension(system: ExtendedActorSystem) = new GitrExtensionImpl(system)
}

class GitrExtensionImpl(system: ExtendedActorSystem) extends Extension {

  val root = Paths.get(Publet(system).settings.config.getString("publet.gitr.root"))

  val repoMan = system.actorOf(Props(new RepositoryManager(root)), name = "repo-man")
}
