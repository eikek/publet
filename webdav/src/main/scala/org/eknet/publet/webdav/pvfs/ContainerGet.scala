package org.eknet.publet.webdav.pvfs

import org.eknet.publet.vfs.{Resource, ContainerResource, Path, Container}
import java.io.{BufferedOutputStream, OutputStream}
import java.util
import io.milton.resource.GetableResource
import io.milton.http
import io.milton.http.{XmlWriter, Auth}
import org.eknet.publet.web.util.{PubletWebContext, PubletWeb}
import org.eknet.publet.web.shiro.Security

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
    val path = PubletWebContext.applicationPath
    val content =
      <html>
        <head><title>Listing</title></head>
        <body>
          <h2>Listing</h2>
          <ul>
            { for (r <- getChildren(path)) yield <li><a href={ r.name.fullName }>{ r.name.fullName }</a></li> }
          </ul>
        </body>
      </html>

    val bout = new BufferedOutputStream(out)
    bout.write(content.toString().getBytes("UTF-8"))
    bout.flush()
  }
}
