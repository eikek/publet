package org.eknet.publet.actor

import akka.actor.{ActorRef, PoisonPill, Props, Actor}
import scala.concurrent.duration._
import akka.util.Timeout
import org.eknet.publet.actor.convert.Converter
import org.eknet.publet.actor.docroot.DocumentRoot
import messages._
import akka.routing.RoundRobinRouter
import akka.actor.Status.Status
import scala.concurrent.Future
import scala.util.{Failure => TFailure, Success => TSuccess, Try}
import akka.actor.Status.Success
import akka.actor.Status.Failure
import scala.collection.Map
import PubletActor._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.05.13 18:14
 */
class PubletActor extends Actor with Logging {

  val settings = Publet(context.system).settings

  // actor components
  val engineRegistry = context.actorOf(Props[Converter], name = "engine-registry")

  private val stopTime = utils.Stopwatch.start()

  def receive = {
    case req @ FindContent(_, _) => {
      context.actorOf(Props[FindContentActor])
        .forward(FindContentReq(engineRegistry, req))
    }
    case req: EngineRegistryMessage => {
      engineRegistry.forward(req)
    }
    case Starting => {
      startPlugins(self)
    }
    case PluginsDone(status) => status match {
      case Success(_) => {
        val publet = Publet(context.system)

        for ((uri, paths) <- settings.mounts) {
          val part = publet.partitionFactory().create(uri)
          for (p <- paths) {
            publet.documentRoot.send(dr => dr.mount(p, part))
          }
        }

        self ! Initialized(Success("Ok"))
      }
      case Failure(e) => self ! Initialized(Failure(e))
    }
    case Initialized(status) => {
      status match {
        case Success(_) => {
          context.system.eventStream.publish(Available(self))
        }
        case Failure(e) => {
          if (settings.stopOnInitError) {
            log.error(e, "Stopping due to startup errors.")
            self ! PoisonPill
          } else {
            log.error(e, "Startup errors occured.")
            log.warning("!!!! Proceed despite startup errors !!!")
            context.system.eventStream.publish(Available(self))
          }
        }
        log.info(s"===== publet ready. started in ${stopTime()}ms =====")
      }
    }
  }

  override def preStart() {
    super.preStart()
    log.info(s"""
               |
               |   ._        |_   |   _   _|_  ${BuildInfo.version}
               |   |_)  |_|  |_)  |  (/_   |_
               |   |
               |            starting
               |
               |""".stripMargin)

    self ! Starting
  }

  /** Starts the plugins in dependency-order by invoking their `load` method */
  private[actor] def startPlugins(actor: ActorRef) {
    import context.dispatcher

    val sorted = settings.pluginsSorted
    def startup(current: Future[_], list: List[List[String]]) {
      list match {
        case Nil => current.onComplete {
          case x => actor ! pluginsDone(x)
        }
        case a::as => current.onComplete  {
          case TSuccess(_) => {
            val plugins = a.map(settings.plugins.apply)
            plugins.foreach(p => log.debug(s"Starting plugin ${p.getClass}: ${p.name} -> ${p.dependsOn}"))
            val f = Try(Future.sequence(plugins.map(_.load(actor, context.system))))
              .recover({ case e => Future.failed(e)})
            startup(f.get, as)
          }
          case TFailure(e) => actor ! PluginsDone(Failure(e))
        }
      }
    }
    sorted match {
      case Right(set) => {
        log.info(s"Starting plugins: ${set.map(_.mkString(", ")).mkString("; ")}")
        startup(Future.successful(""), set)
      }
      case Left(remain) => {
        val msg = s"Unable to resolve plugin dependencies. Remaining: $remain"
        actor ! PluginsDone(Failure(new IllegalStateException(msg)))
      }
    }
  }

}

object PubletActor {

  private case class Initialized(state: Status)

  private case class PluginsDone(state: Status)
  private def pluginsDone(t: Try[_]) = t match {
    case util.Success(_) => PluginsDone(Success("ok"))
    case util.Failure(e) => PluginsDone(Failure(e))
  }
  private case object Starting

}