package org.eknet.publet.content

import scala.util.{Failure, Try}
import scala.annotation.tailrec

/**
 * A partition controls a tree of resources.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.05.13 19:25
 */
trait Partition {

  /**
   * Looks up a resource at the given path.
   *
   * Since a partition also is a folder, the lookup for `EmptyPath`
   * should return the root folder for this partition.
   *
   * @param path
   * @return
   */
  def find(path: Path): Option[Resource]

  /**
   * Lists all resources that match the given path. The path
   * segments may contain wild cards `*` or `?` to match multiple
   * elements.
   *
   * @param path
   * @return
   */
  def select(path: Path): Iterable[(Path, Resource)]

  /**
   * Lists the root children of this partition. This is the
   * same as listing the children of the folder at `Path.root`.
   *
   * @return
   */
  def children: Iterable[Resource]

  /**
   * Creates a new folder resource at the specified path. If the
   * folder resource already exists, it is simply returned. Parent
   * folders are created if necessary.
   *
   * @param path
   * @param info some information as to who did the modification and why
   * @return
   */
  def createFolder(path: Path, info: ModifyInfo): Try[Folder]

  /**
   * Creates a new content resource at the specified path
   * with the given content. The `path` argument denotes
   * the folder where `content` is added to.
   *
   * If a resource exists, it is overwritten silently, otherwise
   * it is created as well as the folder structure if necessary.
   *
   * @param path the folder where the new content is created
   * @param content the data together with a name
   * @param info some information as to who did the modification and why
   * @return
   */
  def createContent(path: Path, content: Content, info: ModifyInfo): Try[Content]

  /**
   * Deletes the resource at the given path. If the path points
   * to a `Folder`, the folder and all its contents are deleted.
   * If the path points to content resource, it is deleted. Empty
   * folders are not removed automatically.
   * If the path points to an non-existing resource, `false` is
   * returned.
   *
   * @param path
   * @return
   */
  def delete(path: Path, info: ModifyInfo): Try[Boolean]
}

case class ModifyInfo(user: String, message: String)
object ModifyInfo {
  val none = ModifyInfo("", "")
}

trait ReadOnlyPartition extends Partition {

  def createContent(path: Path, content: Content, info: ModifyInfo): Try[Content] = readOnlyError
  def delete(path: Path, info: ModifyInfo): Try[Boolean] = readOnlyError
  def createFolder(path: Path, info: ModifyInfo): Try[Folder] = readOnlyError

  private def readOnlyError = Failure(new UnsupportedOperationException("this partition is readonly"))

}

/**
 * Base class for implementing partitions. It is read-only, all writing
 * methods throw exceptions.
 *
 * It implements `children` in terms of `find`. The root children are
 * looked up via a call to `find(Path.root)`.
 *
 * If the trait `PartitionSelect` is mixed in, the only method left to
 * implement is `find`.
 *
 */
trait FindBasedPartition extends ReadOnlyPartition {

  def children: Iterable[Resource] = find(Path.root) match {
    case Some(f: Folder) => f.children
    case _ => sys.error(s"The partition $this has no root folder")
  }

}

/**
 * Base class for implementing partitions. It is read-only, all writing
 * methods throw exceptions.
 *
 * It implements `find` in terms of `children` by recursivley traversing
 * the folder structure for the given path. If `find` could be implemented
 * by a simple lookup, see [[org.eknet.publet.content.FindBasedPartition]]
 * base class.
 *
 * If the trait `PartitionSelect` is mixed in, the only method left to
 * implement is `children`.
 *
 */
trait ChildrenBasedPartition extends ReadOnlyPartition {

  private val thisFolder = this match {
    case f: Folder => f
    case _ => new Folder {
      def children = ChildrenBasedPartition.this.children
      def name = Name("root")
    }
  }

  def find(path: Path) = path match {
    case EmptyPath => Some(thisFolder)
    case _ => recursiveFind(path, children)
  }


  @tailrec
  private final def recursiveFind(path: Path, children: Iterable[Resource]): Option[Resource] = {
    path match {
      case a / EmptyPath => children.find(r => r.name.fullName == a)
      case a / as => children.find(r => r.name.fullName == a) match {
        case Some(f: Folder) => recursiveFind(as, f.children)
        case _ => None
      }
      case EmptyPath => None
    }
  }
}

/**
 * A simple read-only partition for a list of resources.
 *
 * @param children a list of resources
 */
class SimplePartition(val children: Iterable[Resource]) extends ChildrenBasedPartition with PartitionSelect


object EmptyPartition extends FindBasedPartition {
  def find(path: Path) = None
  def select(path: Path) = Nil
}

/**
 * Looks up resources from the class path.
 *
 * @param loader
 * @param base
 */
class ClasspathPartition(loader: ClassLoader = Thread.currentThread().getContextClassLoader, base: String = "") extends FindBasedPartition {

  def find(path: Path) = {
    val name = Path(base) / path
    val url = Option(loader.getResource(name.toString))
    url.map(Resource.forUrl)
  }

  /**
   * Since it is not (reasonable) possible to list resources within the
   * classpath, this partition does not support `select`.
   *
   * @param path
   * @return
   */
  def select(path: Path) = Nil
}