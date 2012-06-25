package org.eknet.publet.web.webdav

import org.eknet.publet.vfs._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 21:34
 */
class WebdavDirectory(val container: ContainerResource) extends DavResource with DavDelResource with DavCollResource with DavContainerResource {
  val resource = container
}
