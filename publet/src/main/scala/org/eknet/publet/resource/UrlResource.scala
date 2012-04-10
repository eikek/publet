package org.eknet.publet.resource

import java.net.URL
import org.eknet.publet.Path

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.04.12 23:14
 */
class UrlResource(val url: Option[URL]) extends ContentResource {
  
  Predef.ensuring(url != null, "url must not be null")
  
  def path = Path(url.get.getPath)

  def parent = None

  def lastModification = url.get.openConnection().getLastModified match {
    case 0 => None
    case x => Some(x)
  }

  def name = path.segments.last

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

  def delete() {
    sys.error("deleting url not supported")
  }

  def create() {
    sys.error("Creating url not supported")
  }

  def inputStream = url.get.openStream()

  def outputStream = Some(url.get.openConnection().getOutputStream)
  
  def length = url.get.openConnection().getContentLength match {
    case -1 => None
    case x => Some(x)
  }

  def contentType = ContentType(path)

  override def toString = "Url["+url+"]"
}
