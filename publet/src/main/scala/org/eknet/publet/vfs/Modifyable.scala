package org.eknet.publet.vfs


/**
 * A modifyable resource
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.04.12 18:25
 */
trait Modifyable {
  this: Resource =>

  /**
   * Deletes this resource. If this is a container
   * it must be empty before deleting it.
   *
   */
  def delete()

  /** Creates this resource, if it does not exist, Does nothing
   * if it already exists.
   *
   * The parent container must exist.
   *
   */
  def create()

}