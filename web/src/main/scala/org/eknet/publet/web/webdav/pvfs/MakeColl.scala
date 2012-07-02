package org.eknet.publet.web.webdav.pvfs

import com.bradmcevoy.http.MakeCollectionableResource
import org.eknet.publet.vfs.{Container, Modifyable}
import org.eknet.publet.web.webdav.{SimpleContainer, WebdavResource}
import com.bradmcevoy.http.exceptions.BadRequestException

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.06.12 23:09
 */
trait MakeColl extends MakeCollectionableResource {
  this: CollResource with DelegateResource[Container] =>

  def createCollection(newName: String) = {
    import SimpleContainer.wrapContainer
    resource.container(newName) match {
      case c: Modifyable => {
        c.create()
        c.asInstanceOf[Container]
      }
      case r@_ => throw new BadRequestException("Resource cannot be created: "+ r)
    }
  }
}