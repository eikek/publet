package org.eknet.publet.vfs

import io.Source
import java.io._
import xml.NodeSeq

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:08
 */
trait Content {

  def contentType: ContentType

  def inputStream: InputStream

  def lastModification: Option[Long] = None

  def outputStream: Option[OutputStream] = None

  def length: Option[Long] = None

  def writeFrom(in: InputStream, message: Option[String] = None) {
    Content.copy(in, outputStream.get, closeIn = false)
  }

  def copyTo(out: OutputStream) {
    Content.copy(inputStream, out, true, false)
  }

  def contentAsString = Source.fromInputStream(inputStream).getLines().mkString("\n")
}

case class NodeContent(node: NodeSeq, contentType: ContentType) extends Content {
  def inputStream = new ByteArrayInputStream(node.toString().getBytes("UTF-8"))

  override def toString = "Content("+ node.toString() + ", type="+ contentType +")"
}

object Content {

  def empty(ct: ContentType) = Content("", ct)

  def apply(file: File, ct: ContentType): Content = new Content {
    def inputStream = new FileInputStream(file);
    override def lastModification = Some(file.lastModified)
    override def outputStream = Some(new FileOutputStream(file))
    val contentType = ct
  }

  def apply(file: File): Content = Content(file, ContentType(file))

  def apply(lines: Iterable[String], ct: ContentType): Content = new Content {
    def inputStream = new ByteArrayInputStream(lines.mkString("\n").getBytes("UTF-8"))
    val contentType = ct
  }

  def apply(str: String, ct: ContentType): Content = new Content {
    def inputStream = new ByteArrayInputStream(str.getBytes("UTF-8"))
    val contentType = ct
    override val contentAsString = str
  }

  def apply(in: InputStream, ct: ContentType): Content = new Content {
    val contentType = ct
    val inputStream = in
  }
  
  def apply(in: Array[Byte], ct: ContentType): Content = new Content {
    def contentType = ct
    def inputStream = new ByteArrayInputStream(in)
  }

//  def apply(url: URL): Content = {
//    val ct = Path(url.getFile).fileName.targetType.get
//    Content(url, ct)
//  }
//
//  def apply(url: URL, ct: ContentType): Content = new Content {
//    val contentType = ct;
//    def lastModification = url.openConnection().getLastModified match {
//      case 0 => None
//      case x => Some(x)
//    }
//    def inputStream = url.openStream()
//  }

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