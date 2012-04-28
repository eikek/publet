package org.eknet.publet.vfs

import java.io.{InputStream, OutputStream}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 19:57
 */
class CompositeContentResource(resource: Resource, content: Content) extends ContentResource {

  def path = resource.path.withExt(contentType.extensions.head)

  /**
   * Returns the parent container. For the
   * root this is invalid and returns `None`
   *
   * @return
   */
  def parent = resource.parent

  /**
   * Tells, whether this resource exists.
   *
   * @return
   */
  def exists = resource.exists

  override def name = resource.name

  override lazy val isRoot = resource.isRoot


  // Content interface


  def contentType = content.contentType

  def inputStream = content.inputStream

  override def lastModification = content.lastModification

  override def outputStream = content.outputStream

  override def length = content.length

  override def writeFrom(in: InputStream) {
    content.writeFrom(in)
  }

  override def copyTo(out: OutputStream) {
    content.copyTo(out)
  }

  override def contentAsString = content.contentAsString
}
