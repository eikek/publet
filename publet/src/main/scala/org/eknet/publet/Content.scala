package org.eknet.publet

import tools.nsc.io.{File, Streamable}
import collection.mutable.ListBuffer
import java.io.{ByteArrayInputStream, InputStream}
import io.Source
import xml.Node

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:08
 */
trait Content {

  def contentType: ContentType

  def content: InputStream
  
  def lastModification: Option[Long]
  
  def contentAsBytes: Array[Byte] = Streamable.bytes(content)
  
  def contentAsString = Source.fromInputStream(content).getLines().mkString("\n")
}

case class FilePage(file: File, contentType: ContentType) extends Content {
  def content = file.inputStream()

  def lastModification = Some(file.lastModified)
}

case class StringPage(str: String, contentType: ContentType) extends Content {
  
  def content = new ByteArrayInputStream(str.getBytes("UTF-8"))

  def lastModification = None
}

case class LinePage(buf: Iterable[String], ct: ContentType) extends StringPage(buf.mkString("\n"), ct)

object Content {

  def apply(file: File, ct: ContentType): Content = new FilePage(file, ct)
  def apply(file: File): Content = Content(file, ContentType(file))

  def apply(lines: Iterable[String], ct: ContentType): Content = new LinePage(lines, ct)
  def apply(str: String, ct: ContentType):Content = StringPage(str, ct)
}