package org.eknet.publet.vfs

/** A versioned resource.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.05.12 01:53
 */
trait Versioned {
  this: Resource =>

  /**
   * Returns current version
   * of this resource.
   *
   * @return
   */
  def currentVersion: Version


  case class Version(id: String)
}
