package org.eknet.publet.vfs.util

import org.eknet.publet.vfs.ContentResource

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.04.12 19:21
 */
trait ForwardingContentResource extends ForwadingResource with ContentResource {

  protected def delegate: ContentResource

  def contentType = delegate.contentType

  def inputStream = delegate.inputStream

  override def lastModification = delegate.lastModification
}
