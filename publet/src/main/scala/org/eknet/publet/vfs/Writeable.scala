package org.eknet.publet.vfs

import java.io.{InputStream, OutputStream}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.05.12 16:19
 */
trait Writeable {
  this: Resource =>

  /**
   * Factory for outputstreams
   * @return
   */
  def outputStream: OutputStream

  def writeFrom(in: InputStream, message: Option[String] = None) {
    Content.copy(in, outputStream, closeIn = false)
  }
}
