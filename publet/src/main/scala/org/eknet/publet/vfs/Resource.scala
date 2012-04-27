package org.eknet.publet.vfs


/**
 * A resource is an abstract named content, like a file on a
 * local or remote file system. It may also be a directory or
 * any other container like resource.
 *
 * It is uniquely identified within a root container by its `path`
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
   * root this is invalid and returns `None`
   *
   * @return
   */
  def parent: Option[Container]

  /**
   * If available, returns the alst modification timestamp
   * of this resource.
   *
   * @return
   */
  def lastModification: Option[Long]

  def name = path.name

  /**
   * Returns whether this is the root of the
   * resource tree.
   *
   * @return
   */
  lazy val isRoot: Boolean = parent.isDefined


  /**
   * Tells, whether this resource exists.
   *
   * @return
   */
  def exists: Boolean

  def map[A](f:Resource.this.type=>Option[A]):Option[A] = {
    if (exists) f(this)
    else None
  }
}

trait ContentResource extends Resource with Content
trait ContainerResource extends Resource with Container

object Resource {

  def isContainer(r:Resource):Boolean = r match {
    case r:Container => true
    case _ => false
  }

  def isContent(r:Resource): Boolean = r match {
    case r:Content => true
    case _ => false
  }

  def emptyContainer(path: Path, parent:Option[Container]):ContainerResource = new EmptyContainer(path, parent)
  def emptyContent(path: Path, parent: Option[Container]): ContentResource = new EmptyContent(path, parent)

  private class EmptyContainer(val path: Path, val parent:Option[Container]) extends ContainerResource {

    def exists = false

    def children = List()

    def content(name: String) = emptyContent(path/name, Some(this))

    def container(name: String) = emptyContainer(path/name, Some(this))

    def child(name: String) = sys.error("Child '"+name+"' does not exist")

    def hasEntry(name: String) = false

    def lastModification = None
  }

  private class EmptyContent(val path: Path, val parent: Option[Container]) extends ContentResource {

    def exists = false

    def contentType = ContentType.unknown

    def inputStream = throw new RuntimeException("no input available")

  }

}