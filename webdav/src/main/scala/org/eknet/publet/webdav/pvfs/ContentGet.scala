package org.eknet.publet.webdav.pvfs

import org.eknet.publet.vfs.ContentResource
import java.io.OutputStream
import java.util
import io.milton.resource.GetableResource
import io.milton.http

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.06.12 23:17
 */
trait ContentGet extends GetableResource {
  this: DelegateResource[ContentResource] =>

  def sendContent(out: OutputStream, range: http.Range, params: util.Map[String, String], contentType: String) {
    resource.copyTo(out, close = true)
  }

  def getMaxAgeSeconds(auth: http.Auth) = null

  def getContentType(accepts: String) = resource.contentType.mimeString

  def getContentLength = resource.length.map(java.lang.Long.valueOf(_)).orNull

}
