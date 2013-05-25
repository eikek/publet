package org.eknet.publet

import org.eknet.publet.content.{Name, Resource, Content}
import spray.http._
import org.parboiled.common.FileUtils
import spray.http.HttpHeaders.`Last-Modified`
import spray.http.HttpResponse

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 15.05.13 06:51
 *
 */
package object webapp {

  def makeResponse(c: Content): HttpResponse = {
    val date = c.lastModification.map(DateTime.apply).getOrElse(DateTime.now)
    val body = HttpBody(
      mimeType(c).map(ContentType.apply).getOrElse(ContentType.`application/octet-stream`),
      FileUtils.readAllBytes(c.inputStream)
    )
    HttpResponse(entity = body, headers = List(`Last-Modified`(date)))
  }

  def makeResponse(oc: Option[Content]): HttpResponse = oc match {
    case Some(c) => makeResponse(c)
    case None => HttpResponse(status = StatusCodes.NotFound)
  }

  private def mimeType(c: Content): Option[MediaType] = {
    val mt = c.contentType match {
      case content.ContentType(base, sub, _) => MediaTypes.getForKey(base -> sub)
      case _ => None
    }
    mt.orElse(mimeType(c.name))
  }

  private def mimeType(name: Name): Option[MediaType] = MediaTypes.forExtension(name.ext)

  case class RequestCycle(req: HttpRequest, resp: HttpResponse, duration: Long)

  case class SettingsReload(settings: ApplicationSettings)
}
