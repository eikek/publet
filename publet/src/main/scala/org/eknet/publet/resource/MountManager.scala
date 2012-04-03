package org.eknet.publet.resource

import collection.mutable
import org.eknet.publet.Path

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 02.04.12 23:01
 */
class MountManager[T] {

  private val mounts = mutable.Map[Path, T]()
  private val tree = mutable.Map[String, List[String]]()

  def mount(path: Path, part: T) {
    Predef.ensuring(path != null, "null")
    Predef.ensuring(part != null, "null")
    if (isMounted(path)) sys.error("Path already mounted")
    else mounts.put(path, part)
    
    if (!path.isRoot) {
      path.segments zip path.segments.tail map {t =>
        tree += t._1 -> (t._2 :: tree.get(t._1).getOrElse(List()))
      }
    }
  }

  def isMounted(path: Path): Boolean = mounts.contains(path)

  def isMounted(p: T => Boolean): Boolean = mounts.values.exists(p)

  def getMountAt(path: Path): Option[T] = mounts.get(path)

  def mountedPaths = mounts.keySet

  def resolveMount(path: Path): Option[(Path, T)] = {
    val list = mounts.keys.toList.sorted
    list.find(a => path.prefixedBy(a)).map(key => key -> mounts.get(key).get)
  }
  
  protected[publet] def pathEndsInMountpoint(p: Path): Boolean = tree.contains(p.segments.last)

  protected[publet] def nextSegments(path: Path): List[String] = tree.get(path.segments.last).getOrElse(List())
}
