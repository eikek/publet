package org.eknet.publet.web.webdav

import com.bradmcevoy.http._
import com.bradmcevoy.http.exceptions.BadRequestException
import java.io.{IOException, OutputStream}
import java.util

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 23:08
 */
trait DavContainerResource extends CopyableResource with MoveableResource with GetableResource {
  this: DavResource =>

  def copyTo(toCollection: CollectionResource, name: String) {
    throw new BadRequestException("Not implemented")
  }

  def moveTo(rDest: CollectionResource, name: String) {
    throw new BadRequestException("Not implemented")
  }

  def getContentLength = null

  def getContentType(accepts: String) = "text/html"

  def getMaxAgeSeconds(auth: Auth) = null

  def sendContent(out: OutputStream, range: Range, params: util.Map[String, String], contentType: String) {
    val w = new XmlWriter(out)
    w.open("hmtl")
    w.open("body")
    w.begin("h1").open().writeText("Sorry, not implemented :(").close()
    w.close("body")
    w.close("html")
    w.flush()
  }
}
