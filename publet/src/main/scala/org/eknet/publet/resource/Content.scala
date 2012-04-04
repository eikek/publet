package org.eknet.publet.resource

import io.Source
import java.net.URL
import java.io._
import org.eknet.publet.Path

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:08
 */
abstract class Content {

  def contentType: ContentType

  def content: InputStream

  def lastModification: Option[Long]

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

  def apply(file: File, ct: ContentType): Content = new Content {
    def content = new FileInputStream(file);
    def lastModification = Some(file.lastModified)
    def output = Some(new FileOutputStream(file))
    val contentType = ct
  }

  def apply(file: File): Content = Content(file, ContentType(file))

  def apply(lines: Iterable[String], ct: ContentType): Content = new Content {
    def content = new ByteArrayInputStream(lines.mkString("\n").getBytes("UTF-8"))
    def output = None
    def lastModification = None
    val contentType = ct
  }

  def apply(str: String, ct: ContentType): Content = new Content {
    def content = new ByteArrayInputStream(str.getBytes("UTF-8"))
    def lastModification = Some(System.currentTimeMillis())
    def output = None
    val contentType = ct
  }

  def apply(in: InputStream, ct: ContentType): Content = new StreamContent(in, ct)

  def apply(url: URL): Content = {
    val ct = Path(url.getFile).targetType.get
    Content(url, ct)
  }

  def apply(url: URL, ct: ContentType): Content = new UrlContent(url, ct)

  def copy(in: InputStream, out: OutputStream) {
    val buff = new Array[Byte](1024)
    var len = 0
    while (len != -1) {
      len = in.read(buff)
      out.write(buff, 0, len)
    }
    out.flush();
  }
}