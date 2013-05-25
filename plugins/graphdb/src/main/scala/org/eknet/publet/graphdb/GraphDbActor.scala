package org.eknet.publet.graphdb

import akka.actor.{ActorRef, Actor}
import org.eknet.publet.actor.Logging
import org.eknet.publet.graphdb.GraphDbActor.{InitDb, GraphUpdate}
import scala.util.{Failure, Try}
import akka.util.Timeout
import scala.reflect.ClassTag
import scala.concurrent.Future

/**
 * Simple actor to encapsulate a graph database. Send an [[org.eknet.publet.graphdb.GraphDbActor.InitDb]]
 * message after creation, such that the database object can be created:
 * {{{
 *   val ref = system.actorOf(Props[GraphDbActor], name = "graphdb")
 *   ref ! InitDb("testdatabase")
 * }}}
 * Then it is ready to receive [[org.eknet.publet.graphdb.GraphDbActor.GraphUpdate]] messages that contain
 * functions to modify/query the database. The result of this function is returned:
 * {{{
 *   val f = (ref ? GraphUpdate { g => val v = g.addVertex(); v.getId.toString }).mapTo[String]
 * }}}
 *
 * Alternatively, `GraphDbActor.pattern.graphUpdate` can be imported to reduce typing:
 * {{{
 *   import GraphDbActor.pattern.graphUpdate
 *   val f = ref <<? { g => val v = g.addVertex(); v.getId.toString }
 * }}}
 *
 * Each `GraphUpdate` command is executed within a db transaction.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.05.13 18:54
 */
class GraphDbActor extends Actor with Logging {

  private var graph: BlueprintsGraph = _

  private def graphApply(f: BlueprintsGraph => Any) = {
    try {
      val result = f(graph)
      graph.commit()
      result
    } catch {
      case ex: Exception => {
        graph.rollback()
        throw ex
      }
    }
  }

  def receive = waitForInit

  def ready: Receive = {
    case GraphUpdate(fun) => Try {
      sender ! graphApply(fun)
    }
    case r @ InitDb(_) => {
      log.error(s"GraphDbActor '${self.path}' already initialized. Disregard message  r!")
    }
  }

  def waitForInit: Receive = {
    case InitDb(name) => {
      graph = GraphdbExt(context.system).getOrCreate(name)
      context become ready
    }
    case GraphUpdate(f) => {
      log.error(s"GraphActor '${self.path}' not initialized! Send an InitDb(name) message first.")
    }
  }
}

object GraphDbActor {

  case class InitDb(name: String)
  case class GraphUpdate(f: BlueprintsGraph => Any)

  object pattern {
    implicit def graphUpdate(ref: ActorRef) = new GraphActorRef(ref)
  }

  final class GraphActorRef(val ref: ActorRef) {
    import akka.pattern.ask

    def <<?[A](f: BlueprintsGraph => A)(implicit tag: ClassTag[A], timeout: Timeout): Future[A] = ask(ref, GraphUpdate(f)).mapTo[A]
    def <<!(f: BlueprintsGraph => Any) { ref ! GraphUpdate(f) }
  }
}
