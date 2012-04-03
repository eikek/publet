package org.eknet.publet.resource

import org.eknet.publet.Path

/**
 * A resource is an abstract named content, like a file on a
 * local or remote file system. It may also be a directory or
 * any other container like resource.
 *
 * It is uniquely identified within one partition by its `path`
 * attribute.
 *
 * A resource may not exist.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 13:59
 */
trait Resource {

  def path: Path

  /**
   * Returns the parent container. For the
   * root this is invalid.
   *
   * @return
   */
  def parent: Option[ContainerResource]

  /**
   * If available, returns the alst modification timestamp
   * of this resource.
   *
   * @return
   */
  def lastModification: Option[Long]

  def name: String

  /**
   * Returns whether this is the root of the
   * resource tree.
   *
   * @return
   */
  def isRoot: Boolean

  /**
   * Tells whether this resource is writeable.
   *
   * @return
   */
  def isWriteable: Boolean

  /**
   * Tells, whether this resource exists.
   *
   * @return
   */
  def exists: Boolean

  /**
   * Deletes this resource. If this is a container
   * it must be empty before deleting it.
   *
   */
  def delete()

  /**
   * Creates this resource, if it does not exist,
   *
   */
  def create()
}
