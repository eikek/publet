package org.eknet.publet.gitr

import akka.actor.{ActorSystem, ActorRef, Props}
import org.eknet.publet.actor.Plugin
import org.eknet.publet.actor.messages.InstallFactory

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.13 01:00
 */
object GitrPlugin extends Plugin {

  import akka.pattern.ask
  import org.eknet.publet.actor.patterns.defaultTimeout

  def name = "gitr"

  def dependsOn = Set()

  def load(ref: ActorRef, system: ActorSystem) = {
    val repoMan = system.actorOf(Props[RepositoryManager], name = "repo-man")
    ref ? InstallFactory(repoMan, Set("git"))
  }
}
