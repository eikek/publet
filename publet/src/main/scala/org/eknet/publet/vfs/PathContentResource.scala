package org.eknet.publet.vfs

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 20:17
 */
class PathContentResource(val path: Path, val parent: Option[Container], content: Content) extends ContentResource {

  def this(path: Path, content: Content) = this(path, None, content)

  def contentType = content.contentType

  def inputStream = content.inputStream

  val exists = true
}
