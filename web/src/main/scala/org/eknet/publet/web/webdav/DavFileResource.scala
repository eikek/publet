package org.eknet.publet.web.webdav

import com.bradmcevoy.http._
import exceptions.{BadRequestException, ConflictException}
import java.util
import java.io.{InputStream, OutputStream}
import java.lang.Long
import org.eknet.publet.vfs.{Writeable, Modifyable, ContentResource}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 23:12
 */
trait DavFileResource extends CopyableResource with GetableResource with MoveableResource with PostableResource with ReplaceableResource {
  this: DavDelResource =>

  def content: ContentResource

  def replaceContent(in: InputStream, length: Long) {
    throw new BadRequestException("not implemented")
  }

  def sendContent(out: OutputStream, range: Range, params: util.Map[String, String], contentType: String) {
    content.copyTo(out)
  }

  def getMaxAgeSeconds(auth: Auth) = null

  def getContentType(accepts: String) = content.contentType.mimeString

  def getContentLength = content.length.map(_.asInstanceOf[Long]).orNull

  def copyTo(toCollection: CollectionResource, name: String) {
    toCollection match {
      case wd: WebdavDirectory => {
        val r = wd.resource.content(name)
        if (r.exists) throw new ConflictException(WebdavResource(r))
        else r match {
          case mr : Modifyable if (mr.isInstanceOf[Writeable]) => {
            mr.create()
            mr.asInstanceOf[Writeable].writeFrom(content.inputStream)
          }
          case _ => throw new BadRequestException("Resource not modifyable: "+r)
        }
      }
      case _ => throw new BadRequestException("Unable to copy resource to unknown collection: "+ toCollection)
    }
  }

  def moveTo(rDest: CollectionResource, name: String) {
    copyTo(rDest, name)
    delete()
  }

  def processForm(parameters: util.Map[String, String], files: util.Map[String, FileItem]) = ""
}
