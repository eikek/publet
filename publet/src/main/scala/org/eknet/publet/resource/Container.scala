package org.eknet.publet.resource

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 11:56
 *
 */
trait Container {

  def children: Iterable[_ <: Resource]

  def content(name: String): ContentResource

  /**
   * Returns a container resource relative to this.
   *
   * @param name
   * @return
   */
  def container(name: String): ContainerResource

  /**
   * Returns an existing child.
   *
   * @param name
   * @return
   */
  def child(name: String): Resource

  /** Returns whether the given name is an existing
   * child.
   *
   * @param name
   * @return
   */
  def hasEntry(name: String): Boolean
}
