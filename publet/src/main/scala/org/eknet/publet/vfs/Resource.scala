package org.eknet.publet.vfs

import java.io.ByteArrayInputStream


/**
 * A resource is an abstract named content, like a file on a
 * local or remote file system. It may also be a directory or
 * any other container like resource.
 *
 * This resource abstraction may point to an non-existing
 * resource.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 13:59
 */
trait Resource {

  /**
   * If available, returns the alst modification timestamp
   * of this resource.
   *
   * @return
   */
  def lastModification: Option[Long]

  /**
   * The name of this resource.
   *
   * @return
   */
  def name: ResourceName

  /**
   * Tells, whether this resource exists.
   *
   * @return
   */
  def exists: Boolean

  /**
   * Applies the specified function to this resource, if `exists`
   * returns `true`. Otherwise returns `None`
   *
   * @param f
   * @tparam A
   * @return
   */
  def map[A](f:Resource.this.type=>Option[A]):Option[A] = {
    if (exists) f(this)
    else None
  }

}

trait ContentResource extends Resource with Content
trait ContainerResource extends Resource with Container

object Resource {

  val resourceComparator = (r1: Resource, r2: Resource) => {
    if (isContainer(r1) && !isContainer(r2)) true
    else if (isContainer(r2) && !isContainer(r1)) false
    else r1.name.compareTo(r2.name) < 0
  }

  def isContainer(r:Resource):Boolean = r match {
    case r:Container => true
    case _ => false
  }

  def isContent(r:Resource): Boolean = r match {
    case r:Content => true
    case _ => false
  }

  def toModifyable(r: Resource): Option[Modifyable] = {
    r match {
      case m:Modifyable=> Some(m)
      case _ => None
    }
  }

  def emptyContainer(name: ResourceName):ContainerResource = new EmptyContainer(name)
  def emptyContent(name: ResourceName, ct: ContentType = ContentType.unknown): ContentResource = new EmptyContent(name, ct)

  private class EmptyContainer(val name: ResourceName) extends ContainerResource {
    import ResourceName._

    def exists = false
    def children = List()
    def content(name: String) = emptyContent(name.rn)
    def container(name: String) = emptyContainer(name.rn)
    def child(name: String) = None
    def lastModification = None
  }

  private class EmptyContent(val name: ResourceName, val contentType: ContentType) extends ContentResource {

    def exists = false
    def inputStream = new ByteArrayInputStream(Array[Byte]())
  }

}