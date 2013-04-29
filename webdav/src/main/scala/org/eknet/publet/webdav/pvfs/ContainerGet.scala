package org.eknet.publet.webdav.pvfs

import org.eknet.publet.vfs.{Path, Container}
import java.io.{BufferedOutputStream, OutputStream}
import java.util
import io.milton.resource.GetableResource
import io.milton.http
import io.milton.http.Auth
import org.eknet.publet.web.util.PubletWeb
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web.template.IncludeLoader

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.06.12 23:16
 */
trait ContainerGet extends GetableResource {
  this: DelegateResource[Container] =>

  def getContentLength = null

  def getContentType(accepts: String) = "text/html"

  def getMaxAgeSeconds(auth: Auth) = null

  private[this] def getChildren(path: Path) = {
      resource.children.filter(r => Security.hasReadPermission((path / r).toAbsolute))
  }

  def sendContent(out: OutputStream, range: http.Range, params: util.Map[String, String], contentType: String) {
    val uri = loader.findMainAllInclude("_webdav-directory-listing").getOrElse(Path("/publet/webdav/listing/_directory-listing.jade"))
    val output = engine.processUri(uri.asString, None, engine.attributes)
    val bout = new BufferedOutputStream(out)
    output.copyTo(bout, close = false)
    bout.flush()
  }

  def engine = PubletWeb.scalateEngine
  def loader = PubletWeb.instance[IncludeLoader].get
}
