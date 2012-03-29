package org.eknet.publet

import java.io.InputStream
import tools.nsc.io.{File, Streamable}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:08
 */
trait Data {

  def contentType: ContentType

  def content: InputStream
  
  def contentAsBytes: Array[Byte] = Streamable.bytes(content)
}

case class FileData(file: File, contentType: ContentType) extends Data {

  def content = file.inputStream()

}

object Data {

  def apply(file: File, ct: ContentType): Data = new FileData(file, ct)
  def apply(file: File): Data = Data(file, ContentType(file))
}