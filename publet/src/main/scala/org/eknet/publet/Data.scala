package org.eknet.publet

import java.io.InputStream
import tools.nsc.io.{File, Streamable}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:08
 */
trait Data {

  def contentType: String

  def content: InputStream
  
  def contentAsBytes: Array[Byte] = Streamable.bytes(content)
}

class FileData(file: File, ct: String) extends Data {

  def contentType = ct

  def content = file.inputStream()

}

object Data {

  def apply(file: File, ct: String): Data = new FileData(file, ct)
  def apply(file: File): Data = Data(file, file.extension)
}