package org.eknet.publet.vfs.util

import java.net.URL
import org.eknet.publet.vfs._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.04.12 23:14
 */
class UrlResource(val url: Option[URL], val name: ResourceName) extends ContentResource {

  def this(url: URL, name: ResourceName) = this(Some(url), name)

  def this(url: URL) = this(Some(url), ResourceName(url.getFile))

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

  override def length = url.flatMap(u => u.openConnection().getContentLength match {
    case -1 => None
    case x => Some(x)
  })

  def contentType = name.targetType

  override def toString = "Url[" + url + "]"
}
