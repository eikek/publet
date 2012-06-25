package org.eknet.publet.web.webdav

import org.eknet.publet.vfs.ContentResource

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 21:50
 */
class WebdavFile(val content: ContentResource) extends DavResource with DavDelResource with DavFileResource {
  val resource = content
}