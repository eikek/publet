package org.eknet.publet.web.webdav.pvfs

import org.eknet.publet.vfs.{Writeable, Modifyable, ContentResource}
import org.eknet.publet.web.webdav.{DavContainerResource, WebdavResource}
import com.bradmcevoy.http.{CollectionResource, CopyableResource}
import com.bradmcevoy.http.exceptions.{BadRequestException, ConflictException}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.06.12 23:23
 */
trait ContentCopy extends CopyableResource {
  this: DelegateResource[ContentResource] =>

  def copyTo(toCollection: CollectionResource, name: String) {
    toCollection match {
      case wd: DavContainerResource => {
        val r = wd.resource.content(name)
        if (r.exists) throw new ConflictException(WebdavResource(r))
        else r match {
          case mr : Modifyable if (mr.isInstanceOf[Writeable]) => {
            mr.create()
            mr.asInstanceOf[Writeable].writeFrom(resource.inputStream)
          }
          case _ => throw new BadRequestException("Resource not modifyable: "+r)
        }
      }
      case _ => throw new BadRequestException("Unable to copy resource to unknown collection: "+ toCollection)
    }
  }

}
