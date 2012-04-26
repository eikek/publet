package org.eknet.publet.vfs.virtual

import java.net.URL
import org.eknet.publet.vfs._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.04.12 23:14
 */
class UrlResource(val url: Option[URL], val parent: Option[Container], val path: Path) extends ContentResource {

  Predef.ensuring(url != null, "url must not be null")

//  def path = parent match {
//    case Some(p) => p.path / new FileName(url.get.getPath).asString
//    case None => Path(url.get.getPath)
//  }

  override def lastModification = url.get.openConnection().getLastModified match {
    case 0 => None
    case x => Some(x)
  }

  def isWriteable = try {
    if (url.isDefined) {
      url.get.openConnection().getOutputStream
      true
    } else {
      false
    }
  } catch {
    case _ => false
  }

  def exists = url.isDefined

  def inputStream = url.get.openStream()

  override def outputStream = if (isWriteable) Some(url.get.openConnection().getOutputStream) else None

  override def length = url.flatMap(u => u.openConnection().getContentLength match {
    case -1 => None
    case x => Some(x)
  })

  def contentType = name.targetType

  override def toString = "Url[" + url + "]"
}
