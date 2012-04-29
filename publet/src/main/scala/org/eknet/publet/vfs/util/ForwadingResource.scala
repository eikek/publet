package org.eknet.publet.vfs.util

import org.eknet.publet.vfs.Resource

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.04.12 19:24
 */
trait ForwadingResource extends Resource {

  protected def delegate: Resource

  def name = delegate.name

  def lastModification = delegate.lastModification

  def exists = delegate.exists

}
