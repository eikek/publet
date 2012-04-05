package org.eknet.publet.resource

import java.io.File
import org.eknet.publet.Path

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.04.12 23:43
 */
trait Partition extends Container {

  def id: Symbol

  /**
   * Looks up the resource as specified by the path
   *
   * @param path
   * @return
   */
  def lookup(path: Path): Option[Resource]

  def newContainer(path: Path): ContainerResource
  def newContent(path: Path): ContentResource
}

object Partition {

  def directory(root: File): FilesystemPartition = directory(root, 'local)
  def directory(root: File, id: Symbol) = new FilesystemPartition(root, id)

  def classpath(root: Path, clazz: Class[_]): ClasspathPartition = classpath(root, clazz, 'classpath)
  def classpath(root: Path, clazz: Class[_], id: Symbol) = new ClasspathPartition(id, clazz, root)

}
