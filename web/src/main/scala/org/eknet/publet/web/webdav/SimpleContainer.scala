package org.eknet.publet.web.webdav

import org.eknet.publet.vfs.Container
import pvfs._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.06.12 23:45
 */
class SimpleContainer(val resource: Container) extends AbstractDavResource
    with DelegateResource[Container]
    with CollResource
    with ContainerGet
    with MakeContent
    with MakeColl

object SimpleContainer {

  implicit def wrapContainer(c: Container) = new SimpleContainer(c)

  implicit def unwrapContainer(c: SimpleContainer) = c.resource

}