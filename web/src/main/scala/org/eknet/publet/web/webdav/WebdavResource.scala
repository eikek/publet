package org.eknet.publet.web.webdav

import org.eknet.publet.vfs
import vfs.{ContainerResource, ContentResource}
import io.milton.resource.Resource

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

}