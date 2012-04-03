package org.eknet.publet.resource

import java.net.URL
import org.eknet.publet.{ContentType, Path}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.04.12 23:14
 */
class UrlResource(val url: URL) extends ContentResource {
  
  Predef.ensuring(url != null, "url must not be null")
  
  def path = Path(url.getPath)

  def parent = None

  def lastModification = url.openConnection().getLastModified match {
    case 0 => None
    case x => Some(x)
  }

  def name = path.segments.last

  def isWriteable = try {
    url.openConnection().getOutputStream
    true
  } catch {
    case _ => false
  }

  def exists = true

  def delete() {
    sys.error("deleting url not supported")
  }

  def create() {
    sys.error("Creating url not supported")
  }

  def inputStream = url.openStream()

  def outputStream = Some(url.openConnection().getOutputStream)
  
  def length = url.openConnection().getContentLengthLong match {
    case -1 => None
    case x => Some(x)
  }

  def contentType = ContentType(path)
  
}
