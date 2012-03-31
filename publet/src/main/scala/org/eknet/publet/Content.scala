package org.eknet.publet

import tools.nsc.io.{File, Streamable}
import collection.mutable.ListBuffer
import java.io.{ByteArrayInputStream, InputStream}
import io.Source
import xml.Node
import java.net.URL

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

case class FileContent(file: File, contentType: ContentType) extends Content {
  def content = file.inputStream()

  def lastModification = Some(file.lastModified)
}

case class StringContent(str: String, contentType: ContentType) extends Content {
  
  def content = new ByteArrayInputStream(str.getBytes("UTF-8"))

  def lastModification = None
}

case class LinesContent(buf: Iterable[String], ct: ContentType) extends StringContent(buf.mkString("\n"), ct)

case class StreamContent(content: InputStream, contentType: ContentType) extends Content {
  def lastModification = None
}

case class UrlContent(url: URL, contentType: ContentType) extends Content {
  def content = url.openStream()

  def lastModification = None
}

object Content {

  def apply(file: File, ct: ContentType): Content = new FileContent(file, ct)
  def apply(file: File): Content = Content(file, ContentType(file))

  def apply(lines: Iterable[String], ct: ContentType): Content = new LinesContent(lines, ct)
  def apply(str: String, ct: ContentType):Content = StringContent(str, ct)
  
  def apply(in: InputStream, ct: ContentType): Content = new StreamContent(in, ct)
  
  def apply(url: URL): Content = {
    val ct = Path(url.getFile).targetType.get
    Content(url, ct)
  }
  def apply(url: URL, ct: ContentType): Content = new UrlContent(url, ct)
}