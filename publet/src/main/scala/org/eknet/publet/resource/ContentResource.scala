package org.eknet.publet.resource

import java.io.{OutputStream, InputStream}
import org.eknet.publet.{Content, ContentType}


/** A resource that has some content associated. It is always
 * readable and optional writeable.
 *
 * @author Eike Kettner eike@eknet.org
 * @since 01.04.12 14:00
 */
trait ContentResource extends Resource {
  self =>

  override val isRoot = false

  def inputStream: InputStream

  def outputStream: Option[OutputStream]

  def length: Option[Long]

  def contentType: ContentType

  def copyTo(out: OutputStream) {
    copy(inputStream, out)
  }

  def writeFrom(in: InputStream) {
    if (!isWriteable) sys.error("Resource '"+path+"' not writeable")
    else {
      val out = outputStream.get
      copy(in, out)
      out.close()
    }
  }
  
  private def copy(in: InputStream, out: OutputStream) {
    val buff = new Array[Byte](1024)
    var len = 0
    val in = inputStream
    while (len != -1) {
      len = in.read(buff)
      out.write(buff, 0, len)
    }
    out.flush();
  }

  val isContainer = false

  def toContent: Content = new Content {
    def contentType = self.contentType

    def output = self.outputStream

    def lastModification = self.lastModification

    def content = self.inputStream
  }
}
