package org.eknet.publet.vfs

/** A container for resources.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 11:56
 *
 */
trait Container {

  /**
   * The child list of this container.
   *
   * @return
   */
  def children: Iterable[_ <: Resource]

  /**
   * Returns a content resource relative to this
   * container. The resource may not exist.
   *
   * @param name
   * @return
   */
  def content(name: String): ContentResource


  /**
   * Returns a container resource relative to this. The
   * resource may exist or not.
   *
   * @param name
   * @return
   */
  def container(name: String): ContainerResource

  /**
   * Returns an child
   *
   * @param name
   * @return
   */
  def child(name: String): Option[Resource]

  /**
   * Looks up a resource at the specified path relative
   * to this container.
   *
   * The default implementation will look for an existing
   * child using the first segment of the path and recursively
   * calls lookup on that child.
   *
   * @param path
   * @return
   */
  def lookup(path: Path): Option[Resource] = {
    path.segments match {
      case a :: Nil => child(a)
      case a :: _ => container(a).map(_.lookup(path.strip))
      case Nil => None
    }
  }
}
