package org.eknet.publet.web.webdav.pvfs

import org.eknet.publet.vfs.{Writeable, ContentResource, Modifyable, Container}
import java.io.InputStream
import java.lang.Long
import org.eknet.publet.web.webdav.WebdavResource
import org.eknet.publet.web.shiro.Security
import com.bradmcevoy.http.PutableResource
import com.bradmcevoy.http.exceptions.BadRequestException

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.06.12 23:11
 */
trait MakeContent extends PutableResource {
  this: CollResource with DelegateResource[Container] =>

  def createNew(newName: String, inputStream: InputStream, length: Long, contentType: String) = {
    val content = resource.content(newName) match {
      case md: Modifyable => {
        md.create()
        md.asInstanceOf[ContentResource]
      }
      case r@_ => throw new BadRequestException("Unable to create new resource: "+ r)
    }

    content match {
      case wc: Writeable => wc.writeFrom(inputStream, Security.changeInfo("webdav upload"))
      case r@_ => throw new BadRequestException("Resource is not writeable: " +r)
    }
    WebdavResource(content)
  }
}
