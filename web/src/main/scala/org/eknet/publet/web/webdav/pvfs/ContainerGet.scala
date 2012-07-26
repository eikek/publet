package org.eknet.publet.web.webdav.pvfs

import org.eknet.publet.vfs.{ContentResource, Path, Container}
import java.io.OutputStream
import java.util
import io.milton.resource.GetableResource
import io.milton.http.{XmlWriter, Auth}
import io.milton.http

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.06.12 23:16
 */
trait ContainerGet extends GetableResource {
  this: DelegateResource[Container] =>

  def getContentLength = null

  def getContentType(accepts: String) = "text/html"

  def getMaxAgeSeconds(auth: Auth) = null

  def sendContent(out: OutputStream, range: http.Range, params: util.Map[String, String], contentType: String) {
    val w = new XmlWriter(out)
    w.open("hmtl")
    w.open("body")
    w.begin("h1").open().writeText("Sorry, not implemented!").close()
    w.close("body")
    w.close("html")
    w.flush()
  }
}
