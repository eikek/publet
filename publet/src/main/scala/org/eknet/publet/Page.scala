package org.eknet.publet

import java.io.InputStream
import tools.nsc.io.{File, Streamable}

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


object Page {

  def apply(file: File, ct: ContentType): Page = new FilePage(file, ct)
  def apply(file: File): Page = Page(file, ContentType(file))

}