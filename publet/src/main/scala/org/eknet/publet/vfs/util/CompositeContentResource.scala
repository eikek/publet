package org.eknet.publet.vfs.util

import java.io.{InputStream, OutputStream}
import org.eknet.publet.vfs.{ContentResource, Content, Resource}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 19:57
 */
class CompositeContentResource(resource: Resource, content: Content) extends ContentResource {

  def exists = resource.exists

  override def name = resource.name


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
