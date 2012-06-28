package org.eknet.publet.web.webdav

import org.eknet.publet.vfs.ContainerResource
import pvfs._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.06.12 23:52
 */
class DavContainerResource(val resource: ContainerResource) extends AbstractDavResource
    with DelegateResource[ContainerResource]
    with ResourceProperties
    with CollResource
    with ContainerGet
    with ResourceDelete
    with MakeContent
    with MakeColl

object DavContainerResource {

  implicit def wrapContainerResource(r: ContainerResource) = new DavContainerResource(r)

  implicit def unwrapContainerResource(r: DavContainerResource) = r.resource

}
