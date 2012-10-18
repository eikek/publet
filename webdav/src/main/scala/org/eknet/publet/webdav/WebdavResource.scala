package org.eknet.publet.webdav

import org.eknet.publet.vfs
import vfs.{ContainerResource, ContentResource}
import com.bradmcevoy.http.Resource
import org.eknet.publet.web.util.PubletWeb

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 21:35
 */
object WebdavResource {

  def apply(r: vfs.Resource): Resource = r match {
    case c: ContentResource => new DavContentResource(c)
    case c: ContainerResource => new DavContainerResource(c)
    case _ => sys.error("Unreachable code!")
  }

  /**
   * Returns the realm name that is used for WebDAV. This is
   * either retrieved from the settings or the value "WebDav Area"
   * is returned as fallback.
   *
   * @return
   */
  def getRealmName = PubletWeb.publetSettings("webdav.realmName").getOrElse("WebDav Area")
}