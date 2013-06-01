package org.eknet.publet.actor

import akka.actor.{Status, ActorRef, Actor}
import scala.concurrent.Future
import org.eknet.publet.content._
import org.eknet.publet.actor.messages._
import akka.util.Timeout
import org.eknet.publet.actor.docroot.ActorResource.DynActorResource
import org.eknet.publet.actor.messages.Conversion
import org.eknet.publet.content.Resource.SimpleContent
import org.eknet.publet.actor.docroot.ActorResource.Params
import scala.Some
import scala.util.{Failure, Success}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.13 16:22
 */
private class FindContentActor extends Actor with Logging {

  import context.dispatcher
  import scala.concurrent.duration._
  import akka.pattern.ask
  import akka.pattern.pipe
  implicit private val timeout: Timeout = 30.seconds

  def receive = {
    case req @ FindContentReq(_, r) => {
      log.debug(s">>> 1. Incoming request: $r")
      val stopTime = utils.Stopwatch.start()
      val f = findContent(req)
      val origin = sender
      f.andThen {
        case Success(s) ⇒ origin ! s
        case Failure(fail) ⇒ origin ! Status.Failure(fail)
      } andThen {
        case result => {
          log.debug(s">>> 3. Returning '${result.map(o => o.map(_.name))}'")
          context.system.eventStream.publish(PubletResponse(req.req, result, stopTime()))
        }
      } andThen {
        case result => context.stop(self)
      }
    }
  }

  private def evaluate(r: Resource, params: Map[String, String]) = {
    r match {
      case a: DynActorResource => a.eval(Params(r.name, params))
      case _ => Resource.evaluate(r, params)
    }
  }

  /**
   * Finds a content resource at the given path, possibly converting
   * other formats into the requested one.
   *
   * @param m
   * @return
   */
  private def findContent(m: FindContentReq): Future[Option[Resource]] = {
    findResource(m) match {
      case (mreq, Some(resource)) => evaluate(resource, mreq.req.params).map(f => f.collect(Resource.toContent)).flatMap {
        or => or match {
          case Some(c) if (mreq.isTargetType(c)) => {
            log.debug(">>> 2. Found resource "+ c.name.fullName)
            Future.successful(Some(c))
          }
          case _ => convert(mreq)
        }
      }
      case (mreq, None) => convert(mreq)
    }
  }

  private def findResource(m: FindContentReq): (FindContentReq, Option[Resource]) = {
    Publet(context.system).documentRoot().find(m.req.path) match {
      case Some(f: Folder) => findResource(m.copy(req = m.req.copy(path = m.req.path / "index.html")))
      case Some(r) => m -> Some(r)
      case None => m.req.path match {
        case EmptyPath => findResource(m.copy(req = m.req.copy(path = m.req.path / "index.html")))
        case _ => m -> None
      }
    }
  }

  /**
   * Creates a future that will lookup all resources with the same base name
   * as the given path and attempts to convert one of them into the requested
   * format. If the resulting resource is a [[org.eknet.publet.content.DynamicContent]]
   * it is evaluated.
   *
   * @param m
   * @return
   */
  private def convert(m: FindContentReq): Future[Option[Content]] = {
    val targetType = m.req.path.fileName.contentType.getOrElse(ContentType.unknown)
    val selectPath = m.req.path.sibling(m.req.path.fileName.withExtension("*"))
    val list = Publet(context.system).documentRoot().select(selectPath)
    log.debug(s">>> 2. Not found. Try to find a conversion for: ${list.map(_._1.fileName).mkString(",")}")

    val conversionReq = Future.sequence(list.map(t => evaluate(t._2, m.req.params))
      .map(f => f.collect({ case Some(c: Content) => c })))
      .map(list => Conversion(m.req, list.toList, targetType))

    conversionReq.flatMap(req => m.engineReg ? req).mapTo[Option[Source]]
      .map(or => or.map(s => SimpleContent(m.req.path.fileName, s)))
  }
}

private case class FindContentReq(engineReg: ActorRef, req: FindContent) {
  def isTargetType(r: Resource) = r match {
    case c: Content => req.path.fileName.matchesType(c.contentType)
    case _ => req.path.fileName.contentType == r.name.contentType
  }
}