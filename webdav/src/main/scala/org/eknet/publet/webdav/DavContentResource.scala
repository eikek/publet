package org.eknet.publet.webdav

import org.eknet.publet.vfs.ContentResource
import pvfs._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.06.12 23:54
 */
class DavContentResource(val resource: ContentResource) extends AbstractDavResource
    with DelegateResource[ContentResource]
    with ResourceProperties
    with ContentGet
    with ContentCopy
    with ContentMove
    with ResourceDelete

object DavContentResource {

  implicit def wrapContentResource(r: ContentResource) = new DavContentResource(r)

  implicit def unwrapContentResource(r: DavContentResource) = r.resource

}