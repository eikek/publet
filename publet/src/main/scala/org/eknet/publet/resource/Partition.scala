package org.eknet.publet.resource

import org.eknet.publet.{ContentType, Content, Path}
import java.io.File

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

  def hasEntry(path: Path): Boolean = lookup(path).isDefined

  def createContent(path: Path): ContentResource

  def createContainer(path: Path): ContainerResource

}

object Partition {

  def directory(root: File) = new FilesystemPartition(root, 'local)

  def classpath(root: Path, clazz: Class[_]) = new ClasspathPartition('classpath, clazz, root)

  lazy val yamlPartition = classpath(Path("../themes/yaml"), classOf[Partition])

  lazy val highlightPartition = classpath(Path("../themes/highlight"), classOf[Partition])
}
