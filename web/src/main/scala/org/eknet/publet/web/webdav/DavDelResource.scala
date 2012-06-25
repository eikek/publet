package org.eknet.publet.web.webdav

import com.bradmcevoy.http.DeletableResource
import org.eknet.publet.vfs.Modifyable
import com.bradmcevoy.http.exceptions.BadRequestException

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 22:59
 */
trait DavDelResource extends DeletableResource {
  this: DavResource =>

  def delete() {
    resource match {
      case wr: Modifyable => wr.delete()
      case _ => throw new BadRequestException("Resource not writeable: "+ resource)
    }
  }
}
