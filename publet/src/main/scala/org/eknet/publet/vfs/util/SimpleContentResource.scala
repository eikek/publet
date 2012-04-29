package org.eknet.publet.vfs.util

import org.eknet.publet.vfs.{ContentResource, Content, ResourceName}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 20:17
 */
class SimpleContentResource(val name: ResourceName, content: Content) extends ContentResource {

  def contentType = content.contentType

  def inputStream = content.inputStream

  override def lastModification = content.lastModification

  override def outputStream = content.outputStream

  override def length = content.length

  override def contentAsString = content.contentAsString

  val exists = true
}
