package org.eknet.publet.content

import scala.collection.mutable.ListBuffer
import scala.annotation.tailrec

/**
 * Implements `select()` in terms of `find`. It uses `find` quite frequently,
 * so if it performs too slowly, improve it or implement a more specialized
 * version for a certain partition.
 *
 * Otherwise this trait can be mixed in any custom partition implementation
 * to get the `select` method.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.05.13 22:48
 */
trait PartitionSelect {
  this: Partition =>

  private def hasWildcard(s: String) = s.contains('*') || s.contains('?')
  private def isWildcard(buf: List[String]) = buf.size == 1 && hasWildcard(buf.head)

  def select(path: Path): Iterable[(Path, Resource)] = {
    val grouped = splitPath(path.segments)
    select(grouped, Set(Path.root)).map(p => p -> find(p).get)
  }

  /**
   * The `path` argument is a path grouped around segments with wildcards.
   * This path is consumed from the beginning, while a wildcard segment is matched
   * against the child list of the current paths (`result`). A non-wildcard
   * segment is added to the intermediate result paths and they are looked up
   * to filter only those that point to existing resources.
   *
   * @param path a grouped path obtained via `splitPath`
   * @param result the result list, `Path.root` at the beginning
   * @return
   */
  @tailrec
  private def select(path: List[List[String]], result: Set[Path]): Set[Path] = {
    if (path.isEmpty || result.isEmpty) result else {
      val next = path match {
        case a::as if (isWildcard(a)) => {
          val glob = Glob(a.head)
          result.flatMap(p => find(p) match { //find children of current paths
            case Some(f: Folder) => {
              f.children.filter(c => glob.matches(c.name.fullName)).map(c => p / c.name)
            }
            case _ => Nil
          })
        }
        case a::as if (!isWildcard(a)) => {
          //append next path and filter on existing resources
          result.map(p => p / Path(a))
            .filter(p => find(p).isDefined)
        }
      }
      select(path.tail, next)
    }
  }

  /**
   * Splits a list of strings into groups around wildcard segments. Example
   * {{{
   *   scala> splitPath(List("ab", "cd", "de", "*", "ef", "*.mpg"))
   *   res1: List[List[String]] = List(List(ab, cd, de), List(*), List(ef), List(*.mpg))
   * }}}
   *
   * @param segments
   * @return
   */
  private def splitPath(segments: List[String]): List[List[String]] = {
    val buffer = segments.foldLeft(ListBuffer[ListBuffer[String]]())((buf, seg) => {
      if (buf.isEmpty) {
        buf += ListBuffer(seg)
      } else {
        (hasWildcard(seg), hasWildcard(buf.last.last)) match {
          case (false, false) => buf.last += seg
          case _ => buf += ListBuffer(seg)
        }
      }
      buf
    })
    buffer.map(_.toList).toList
  }

}
