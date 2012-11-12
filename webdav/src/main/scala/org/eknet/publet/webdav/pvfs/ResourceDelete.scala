package org.eknet.publet.webdav.pvfs

import org.eknet.publet.vfs.{Modifyable, Resource}
import io.milton.resource.DeletableResource
import io.milton.http.exceptions.BadRequestException

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.06.12 23:13
 */
trait ResourceDelete extends DeletableResource {
  this: DelegateResource[Resource] =>

  def delete() {
    resource match {
      case wr: Modifyable => wr.delete()
      case _ => throw new BadRequestException("Resource not writeable: "+ resource)
    }
  }

}
