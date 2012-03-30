package org.eknet.publet

import tools.nsc.io.{File, Streamable}
import collection.mutable.ListBuffer
import java.io.{ByteArrayInputStream, InputStream}
import io.Source

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

case class StringPage(str: String, contentType: ContentType) extends Page {
  
  def content = new ByteArrayInputStream(str.getBytes("UTF-8"))

  def lastModification = None
}

case class LinePage(buf: Iterable[String], ct: ContentType) extends StringPage(buf.mkString("\n"), ct)
  

object Page {

  def apply(file: File, ct: ContentType): Page = new FilePage(file, ct)
  def apply(file: File): Page = Page(file, ContentType(file))

  def apply(lines: Iterable[String], ct: ContentType): Page = new LinePage(lines, ct)
  def apply(str: String, ct: ContentType):Page = StringPage(str, ct)
}