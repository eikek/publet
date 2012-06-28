package org.eknet.publet.web.webdav.pvfs

import com.bradmcevoy.http.{ReplaceableResource, CopyableResource, CollectionResource, MoveableResource}
import org.eknet.publet.vfs.ContentResource

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
