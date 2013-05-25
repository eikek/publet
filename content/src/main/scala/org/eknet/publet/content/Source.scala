package org.eknet.publet.content

import java.io.{ByteArrayInputStream, InputStream}
import java.net.URL
import org.eknet.publet.content.Source.SourceWithType
import scala.xml.{NodeSeq, XML, Node}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.05.13 22:16
 */
trait Source {

  def inputStream: InputStream
  def contentType: ContentType = ContentType.unknown
  def length: Option[Long] = None
  def lastModification: Option[Long] = None
  def withType(ct: ContentType) = SourceWithType(this, ct)
}

object Source {

  def unapply(s: Source): Option[ContentType] = Some(s.contentType)

  case class EmptySource(override val contentType: ContentType) extends Source {
    def inputStream = new InputStream {
      def read() = -1
    }

    override def length: Option[Long] = Some(0L)
  }
  object EmptySource {
    def apply(): Source = EmptySource(ContentType.unknown)
  }

  case class ByteArraySource(bytes: Array[Byte], override val contentType: ContentType, override val lastModification: Option[Long] = None) extends Source {
    def inputStream = new ByteArrayInputStream(bytes)
    override def length = Some(bytes.length.toLong)
  }
  case class UrlSource(url: URL) extends Source {
    def inputStream = url.openStream()
    override def contentType = Path(url.getPath).fileName.contentType.getOrElse(ContentType.unknown)
  }

  case class StringSource(str: String, override val contentType: ContentType = ContentType.`text/plain`, override val lastModification: Option[Long] = None) extends Source {
    def inputStream = new ByteArrayInputStream(str.getBytes(Charsets.utf8))
    override def length = Some(str.length.toLong)
  }

  final case class XmlSource(xml: Seq[Node]) extends Source {
    private lazy val string = NodeSeq.fromSeq(xml).toString()
    def inputStream = new ByteArrayInputStream(string.getBytes(Charsets.utf8))
    override def contentType = ContentType.`application/xml`
    override def length = Some(string.length.toLong)
  }

  case class SourceWithType(source: Source, override val contentType: ContentType) extends Source {
    def inputStream = source.inputStream
    override def length = source.length
    override def lastModification = source.lastModification
  }

  // some shortcuts

  def html(str: String) = StringSource(str, ContentType.`text/html`)
  def html(node: Seq[Node]) = XmlSource(node).withType(ContentType.`text/html`)

  def css(str: String) = StringSource(str, ContentType.`text/css`)
  def js(str: String) = StringSource(str, ContentType.`application/javascript`)
  def json(str: String) = StringSource(str, ContentType.`application/json`)
  def markdown(str: String) = StringSource(str, ContentType.`text/x-markdown`)
  def textile(str: String) = StringSource(str, ContentType.`text/x-textile`)
  def scala(str: String) = StringSource(str, ContentType.`text/x-scala`)
}