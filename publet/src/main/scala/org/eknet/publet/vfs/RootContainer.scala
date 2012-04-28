package org.eknet.publet.vfs

import java.lang.reflect.{Method, InvocationHandler}
import scala.Array


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
      case None => if (path == Path.root) Some(this) else super.lookup(path)
    }
  }

  val exists = true


  val lastModification = None

  private def toContainer(child:SegTree): ContainerResource = new Inner(path / child.seg, Some(this), child)

  class Inner(val path: Path, val parent: Option[Container], node: SegTree) extends ContainerResource {

    def children = resolveMount(path).map(_._2.children).getOrElse(node.children.map(toContainer))

    def content(name: String) = {
      val empty = Resource.emptyContent(path/name, Some(this))
      if (!isLeaf) empty else {
        resolveMount(path).map(_._2.content(name)).getOrElse(empty)
      }
    }

    def container(name: String) = {
      val empty = Resource.emptyContainer(path/name, Some(this))
      (if (!isLeaf) node.children.find(_.seg == name).map(toContainer)
       else resolveMount(path).map(_._2.container(name)))
        .getOrElse(empty)
    }

    def child(name: String) = if (!isLeaf) {
      node.children.find(_.seg == name).map(toContainer)
    } else {
      resolveMount(path).flatMap(_._2.child(name))
    }

    def isLeaf = node.children.isEmpty

    def lastModification = if (!isLeaf) None else {
      resolveMount(path) match {
        case Some(x) if (x._2.isInstanceOf[Resource]) => x._2.asInstanceOf[Resource].lastModification
        case _ => None
      }
    }

    //TODO use resource name only and build path on demand
//    private def proxy[A<:Resource:Manifest](r: A):A = {
//      if (r.isInstanceOf[ContainerResource]) {
//        new ForwardingContainerResource {
//          def delegate = r.asInstanceOf[ContainerResource]
//          override def parent = Inner.this.parent
//        }.asInstanceOf[A]
//      } else {
//        new ForwardingContentResource {
//          def delegate = r.asInstanceOf[ContentResource]
//          override def parent = Inner.this.parent
//        }.asInstanceOf[A]
//      }
//    }

    val exists = true
  }
}
