package org.eknet.publet.vfs

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.04.12 19:35
 */
trait RootContainer[T<:Container] extends ContainerResource {
  this: MountManager[T] =>

  private def mountedChildren = resolveMount(Path.root) match {
    case Some(p) => p._2.children
    case None => List()
  }

  def children = mountedChildren ++ tree.children.map(toContainer)

  def content(name: String) = child(name) match {
    case Some(c:ContentResource) => c
    case None => Resource.emptyContent(path/name, Some(this))
    case a @ _ => sys.error("Child is not a content resource: "+ a)
  }

  def container(name: String) = child(name) match {
    case Some(c:ContainerResource) => c
    case None => Resource.emptyContainer(path/name, Some(this))
    case a @ _ => sys.error("Child is not a container: "+ a)
  }

  def child(name: String) = mounts.get(Path.root) match {
    case Some(t) => t.child(name)
    case None => tree.children.find(_.seg == name).map(toContainer)
  }

  override def lookup(path: Path): Option[Resource] = {
    resolveMount(path) match {
      case Some(t) => t._2.lookup(path.strip(t._1))
      case None => super.lookup(path)
    }
  }

  val exists = true


  val lastModification = None

  private def toContainer(child:SegTree): ContainerResource = new Inner(path / child.seg, Some(this), child)

  class Inner(val path: Path, val parent: Option[Container], node: SegTree) extends ContainerResource {

    def children = node.children.map(toContainer)

    def content(name: String) = Resource.emptyContent(path/name, Some(this))

    def container(name: String) = node.children.find(_.seg == name).map(toContainer)
      .getOrElse(Resource.emptyContainer(path/name, Some(this)))

    def child(name: String) = node.children.find(_.seg == name).map(toContainer)


    def lastModification = None

    val exists = true
  }
}
