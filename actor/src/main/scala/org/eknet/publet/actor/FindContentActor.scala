package org.eknet.publet.actor

import akka.actor.{ActorRef, Actor}
import scala.concurrent.Future
import org.eknet.publet.content._
import org.eknet.publet.actor.messages._
import akka.util.Timeout
import org.eknet.publet.actor.docroot.ActorResource.DynActorResource
import org.eknet.publet.actor.messages.Conversion
import org.eknet.publet.content.Resource.SimpleContent
import org.eknet.publet.actor.docroot.ActorResource.Params
import scala.Some
import org.eknet.publet.actor.messages.Select
import org.eknet.publet.actor.messages.Find

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
    case req @ FindContentReq(_, _, r) => {
      log.debug(s">>> 1. Incoming request: $r")
      val stopTime = patterns.Stopwatch.start()
      val f = findContent(req)
      f.onComplete {
        case result => {
          log.debug(s">>> 3. Returning '$result'")
          context.system.eventStream.publish(PubletResponse(req.req, result, stopTime()))
        }
      }
      f pipeTo sender
    }
  }

  private def evaluate(r: Resource, params: Map[String, String]) = {
    r match {
      case a: DynActorResource => a.eval(Params(r.name, params))
      case r => Resource.evaluate(r, params)
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
    val resource = ask(m.contentTree, Find(m.req.path)).mapTo[Option[Resource]]
      .flatMap(t => t match {
      case Some(r) => evaluate(r, m.req.params)
      case None => Future.successful(None)
    })
      .map(f => f.collect({ case c: Content => c }))

    resource.flatMap(or => or match {
      case Some(c) if (m.isTargetType(c)) => {
        log.debug(">>> 2. Found resource "+ c.name.fullName)
        Future.successful(Some(c))
      }
      case _ => convert(m)
    })
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
    val list = ask(m.contentTree, Select(m.req.path.parent / m.req.path.fileName.withExtension("*")))
        .mapTo[Iterable[(Path, Resource)]]
    list.onSuccess {
      case x => log.debug(s">>> 2. Not found. Try to find a conversion for: ${x.map(_._1.fileName).mkString(",")}")
    }

    val conversionReq = list.flatMap { iter =>
      Future.sequence(iter.map(t => evaluate(t._2, m.req.params)))
        .map(f => f.collect({ case Some(c: Content) => c }))
        .map(list => Conversion(m.req.path, list.toList, targetType))
    }

    conversionReq.flatMap(req => m.engineReg ? req).mapTo[Option[Source]]
      .map(or => or.map(s => SimpleContent(m.req.path.fileName, s)))
  }
}

private case class FindContentReq(contentTree: ActorRef, engineReg: ActorRef, req: FindContent) {
  def isTargetType(r: Resource) = r match {
    case c: Content => req.path.fileName.matchesType(c.contentType)
    case _ => req.path.fileName.contentType == r.name.contentType
  }
}