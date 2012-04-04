package org.eknet.publet.resource

import io.Source
import java.net.URL
import java.io._
import org.eknet.publet.Path
import xml.NodeSeq

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:08
 */
trait Content {

  def contentType: ContentType

  def inputStream: InputStream

  def lastModification: Option[Long]

  def copyTo(out: OutputStream) {
    Content.copy(inputStream, out, true, false)
  }

  def contentAsString = Source.fromInputStream(inputStream).getLines().mkString("\n")
}

case class NodeContent(node: NodeSeq, contentType: ContentType) extends Content {
  def inputStream = new ByteArrayInputStream(node.toString().getBytes("UTF-8"))
  lazy val lastModification = Some(System.currentTimeMillis())

  override def toString = "Content("+ node.toString() + ", type="+ contentType +")"
}

object Content {

  def apply(file: File, ct: ContentType): Content = new Content {
    def inputStream = new FileInputStream(file);
    def lastModification = Some(file.lastModified)
    def output = Some(new FileOutputStream(file))
    val contentType = ct
  }

  def apply(file: File): Content = Content(file, ContentType(file))

  def apply(lines: Iterable[String], ct: ContentType): Content = new Content {
    def inputStream = new ByteArrayInputStream(lines.mkString("\n").getBytes("UTF-8"))
    val lastModification = None
    val contentType = ct
  }

  def apply(str: String, ct: ContentType): Content = new Content {
    def inputStream = new ByteArrayInputStream(str.getBytes("UTF-8"))
    val lastModification = Some(System.currentTimeMillis())
    val contentType = ct
  }

  def apply(in: InputStream, ct: ContentType): Content = new Content {
    val contentType = ct
    val lastModification = Some(System.currentTimeMillis())
    val inputStream = in
  }

  def apply(url: URL): Content = {
    val ct = Path(url.getFile).targetType.get
    Content(url, ct)
  }
  
  def apply(url: URL, ct: ContentType): Content = new Content {
    val contentType = ct;
    def lastModification = url.openConnection().getLastModified match {
      case 0 => None
      case x => Some(x)
    }
    def inputStream = url.openStream()
  }

  protected[publet] def copy(in: InputStream, out: OutputStream, closeOut: Boolean = true, closeIn: Boolean = true) {
    val buff = new Array[Byte](2048)
    var len = 0
    try {
      while (len != -1) {
        len = in.read(buff)
        if (len != -1) {
          out.write(buff, 0, len)
        }
      }
      out.flush();
    } finally {
      if (closeOut) out.close()
      if (closeIn) in.close()
    }
  }
}