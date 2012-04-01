package org.eknet.publet

import tools.nsc.io.{File, Streamable}
import collection.mutable.ListBuffer
import io.Source
import xml.Node
import java.net.URL
import java.io.{FileOutputStream, OutputStream, ByteArrayInputStream, InputStream}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:08
 */
abstract class Content {

  def contentType: ContentType

  def content: InputStream

  /**
   * If this is writeable, return the output stream to write to.
   *
   * @return
   */
  def output: Option[OutputStream]

  def lastModification: Option[Long]
  
  //def contentAsBytes: Array[Byte] = Streamable.bytes(content)
  def copyTo(out: OutputStream) {
    val buff = new Array[Byte](1024)
    var len = 0
    val in = content
    while (len != -1) {
      len = in.read(buff)
      out.write(buff, 0, len)
    }
    out.flush();
    out.close();
  }
  
  def contentAsString = Source.fromInputStream(content).getLines().mkString("\n")
}

case class FileContent(file: File, contentType: ContentType) extends Content {
  def content = file.inputStream()

  def lastModification = Some(file.lastModified)

  def output = Some(file.outputStream())
}

case class StringContent(str: String, contentType: ContentType) extends Content {
  
  def content = new ByteArrayInputStream(str.getBytes("UTF-8"))

  def lastModification = None

  def output = None
}

case class LinesContent(buf: Iterable[String], ct: ContentType) extends StringContent(buf.mkString("\n"), ct)

case class StreamContent(content: InputStream, contentType: ContentType) extends Content {
  def lastModification = None

  def output = None
}

case class UrlContent(url: URL, contentType: ContentType) extends Content {
  def content = url.openStream()

  def lastModification = None

  def output = Some(url.openConnection().getOutputStream)
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