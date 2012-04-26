package org.eknet.publet.vfs

import collection.mutable
import org.eknet.publet.impl.Conversions._

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 02.04.12 23:01
 */
trait MountManager[T <: Container] {

  private val mountMap = mutable.Map[Path, T]()
  protected val tree = SegTree("/")

  def mount(path: Path, part: T) {
    Predef.ensuring(path != null, "null")
    Predef.ensuring(part != null, "null")
    if (isMounted(path)) throwException("Path already mounted")
    else mountMap.put(path, part)
    
    if (!path.isRoot) tree.add(path)
  }

  def isMounted(path: Path): Boolean = mountMap.contains(path)

  def isMounted(p: T => Boolean): Boolean = mountMap.values.exists(p)

  def mounts = mountMap.toMap

  def resolveMount(path: Path): Option[(Path, T)] = {
    val list = mountMap.keys.toList.sorted
    list.find(a => path.prefixedBy(a)).map(key => key -> mountMap.get(key).get)
  }
  
  case class SegTree(seg:String, children: mutable.ListBuffer[SegTree] = mutable.ListBuffer()) {

    def add(path:Path) {
      path.segments match {
        case a ::_ => children.find(_.seg == a).getOrElse(addChild(a)).add(path.tail)
        case Nil =>
      }
    }

    def addChild(seg:String):SegTree = {
      val node = SegTree(seg)
      children.append(node)
      node
    }

    def includes(path:Path):Boolean = {
      path.segments match {
        case a :: _ => children.find(_.seg == a).map(_.includes(path.tail)).isDefined
        case Nil => false
      }
    }

    def get(path:Path): Option[SegTree] = {
      path.segments match {
        case a :: _ => children.find(_.seg == a)
        case Nil => None
      }
    }
  }
}
