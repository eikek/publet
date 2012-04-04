package org.eknet.publet.resource

import java.io.File
import org.eknet.publet.Path

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.04.12 23:43
 */
trait Partition {

  def id: Symbol

  /**
   * Looks up the resource as specified by the path
   *
   * @param path
   * @return
   */
  def lookup(path: Path): Option[Resource]

  /**
   * Returns the children of this partition.
   *
   * @return
   */
  def children: Iterable[_ <: Resource]

  /** Returns whether the given path maps to an existing
   * resource.
   *
   * @param path
   * @return
   */
  def hasEntry(path: Path): Boolean = lookup(path).isDefined

  /** Creates a new content resource at the specified path.
   * The resource must not exist.
   *
   * @param path
   * @return
   */
  def createContent(path: Path): ContentResource

  /** Creates a new container resource at the specified path.
   * The resource must not exist.
   *
   * @param path
   * @return
   */
  def createContainer(path: Path): ContainerResource

}

object Partition {

  def directory(root: File): FilesystemPartition = directory(root, 'local)
  def directory(root: File, id: Symbol) = new FilesystemPartition(root, id)

  def classpath(root: Path, clazz: Class[_]): ClasspathPartition = classpath(root, clazz, 'classpath)
  def classpath(root: Path, clazz: Class[_], id: Symbol) = new ClasspathPartition(id, clazz, root)

}
