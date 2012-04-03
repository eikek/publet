package org.eknet.publet.resource

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 14:00
 */
trait ContainerResource extends Resource {

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
  def child(name: String): Option[Resource]

  val isContainer = true
}
