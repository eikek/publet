package org.eknet.publet.webdav.pvfs

import org.eknet.publet.vfs.ContentResource
import io.milton.resource.{CollectionResource, CopyableResource, MoveableResource}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.06.12 23:21
 */
trait ContentMove extends MoveableResource {
  this: DelegateResource[ContentResource] with CopyableResource with ResourceDelete =>

  def moveTo(rDest: CollectionResource, name: String) {
    copyTo(rDest, name)
    delete()
  }
}
