package org.eknet.publet.resource

import java.io.{OutputStream, InputStream}

/** A resource that has some content associated. It is always
 * readable and optional writeable.
 *
 * @author Eike Kettner eike@eknet.org
 * @since 01.04.12 14:00
 */
trait ContentResource extends Resource with Content {
  self =>

  override val isRoot = false

  def outputStream: Option[OutputStream]

  def length: Option[Long]

  def writeFrom(in: InputStream) {
    Content.copy(in, outputStream.get, closeIn = false)
  }
  
  val isContainer = false

}
