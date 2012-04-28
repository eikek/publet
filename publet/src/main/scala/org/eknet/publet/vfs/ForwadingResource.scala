package org.eknet.publet.vfs

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.04.12 19:24
 */
trait ForwadingResource extends Resource {

  protected def delegate: Resource

  def path = delegate.path
  def parent = delegate.parent
  def lastModification = delegate.lastModification
  def exists = delegate.exists

}
