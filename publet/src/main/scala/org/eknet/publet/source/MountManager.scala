package org.eknet.publet.source

import scala.collection._
import org.eknet.publet.Path

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 30.03.12 21:00
 */
trait MountManager[T] {

  private val mounts = mutable.Map[Path, T]()

  def mount(path: Path, part: T) {
    Predef.ensuring(path != null, "null")
    Predef.ensuring(part != null, "null")
    if (isMounted(path)) sys.error("Path already mounted")
    else mounts.put(path, part)
  }

  def isMounted(path: Path): Boolean = mounts.contains(path)

  def isMounted(p: T => Boolean): Boolean = mounts.values.exists(p)

  def getMountAt(path: Path): Option[T] = mounts.get(path)
  
  def resolveMount(path: Path): Option[(Path, T)] = {
    val list = mounts.keys.toList.sorted
    list.find((a) => path.prefixedBy(a)).map(key => (key, mounts.get(key).get))
  }

}
