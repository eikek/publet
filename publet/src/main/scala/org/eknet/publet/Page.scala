package org.eknet.publet

import java.io.InputStream
import tools.nsc.io.{File, Streamable}
import java.net.URL

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:08
 */
trait Page {

  def contentType: ContentType

  def content: InputStream
  
  def lastModification: Option[Long]
  
  def contentAsBytes: Array[Byte] = Streamable.bytes(content)
}

case class FilePage(file: File, contentType: ContentType) extends Page {
  def content = file.inputStream()

  def lastModification = Some(file.lastModified)
}

case class UrlPage(url: URL, contentType: ContentType) extends Page {
  def content = url.openStream()

  def lastModification = None
}

object Page {

  def apply(file: File, ct: ContentType): Page = new FilePage(file, ct)
  def apply(file: File): Page = Page(file, ContentType(file))

  def apply(url: URL, ct: ContentType): Page = {
    if (url.getProtocol == "file") FilePage(File(Uri(url.toURI).path), ct)
    else new UrlPage(url, ct)
  }
  def apply(url: URL): Page = Page(url, Uri(url.toURI).targetType.get)
}