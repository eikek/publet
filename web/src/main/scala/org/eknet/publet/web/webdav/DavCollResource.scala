package org.eknet.publet.web.webdav

import com.bradmcevoy.http.{PutableResource, MakeCollectionableResource, CollectionResource}
import org.eknet.publet.vfs.{Writeable, ContentResource, Modifyable, Container}
import com.bradmcevoy.http.exceptions.BadRequestException
import java.io.InputStream
import java.lang.Long

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 23:00
 */
trait DavCollResource extends CollectionResource with MakeCollectionableResource with PutableResource {
  import collection.JavaConversions._

  def container: Container

  def child(childName: String) = container.child(childName).map(WebdavResource(_)).orNull

  def getChildren = container.children.map(WebdavResource(_)).toList

  def createCollection(newName: String) = {
    container.container(newName) match {
      case c: Modifyable => {
        c.create()
        WebdavResource(c.asInstanceOf[Container])
      }
      case r@_ => throw new BadRequestException("Resource cannot be created: "+ r)
    }
  }

  def createNew(newName: String, inputStream: InputStream, length: Long, contentType: String) = {
    val content = container.content(newName) match {
      case md: Modifyable => {
        md.create()
        md.asInstanceOf[ContentResource]
      }
      case r@_ => throw new BadRequestException("Unable to create new resource: "+ r)
    }

    content match {
      case wc: Writeable => wc.writeFrom(inputStream, Some("webdav upload"))
      case r@_ => throw new BadRequestException("Resource is not writeable: " +r)
    }
    WebdavResource(content)
  }
}
