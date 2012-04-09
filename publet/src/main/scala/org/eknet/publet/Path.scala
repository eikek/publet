package org.eknet.publet

import collection.immutable.List
import java.io.File

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 30.03.12 18:41
 */
case class Path(segments: List[String], absolute: Boolean) extends Ordered[Path] {

  def parent = Path(segments.take(segments.length-1), absolute)
  
  def strip = Path(segments.tail, absolute)

  def strip(p: Path): Path = {
    if (prefixedBy(p)) Path(segments.slice(p.size, size), absolute)
    else this
  }
  
  lazy val isRoot = segments.isEmpty

  def child(name: String) = Path(segments ::: List(name), absolute)

  def sibling(name: String) = parent.child(name)
  
  lazy val asString = (if (absolute) "/" else "") + segments.mkString(String.valueOf(Path.sep))
  
  lazy val size = segments.length

  lazy val fileName = new FileName(segments.last)

  /**
   * Returns a concatenation of '../' up to the root
   *
   */
  lazy val relativeRoot = ("../" * parent.size)

  def prefixedBy(p: Path): Boolean = {
    if (p.size > size) false
    else segments.slice(0, p.size) == p.segments
  }  
  
  def toAbsolute = if (absolute) this else Path(segments, true)
  
  def toRelative = if (!absolute) this else Path(segments, false)

  def / (p: Path) = Path(segments ++ p.segments, absolute)

  def / (s: String) = child(s)

  def compare(that: Path) = that.size - size
}

object Path {

  val root = Path(List(), true)

  private val sep = '/'
  
  def apply(str: String): Path = {
    Predef.ensuring(str.length() > 0)
    if (str == "/") root
    else {
      val segs = str.split(sep)
      val abs = str.charAt(0) == sep
      if (abs) new Path(segs.tail.toList, abs)
      else new Path(segs.toList, abs)
    }
  }
  
  def apply(f: File): Path = Path(f.toURI.getPath)
  
  implicit def toFile(p: Path): File = new File(p.asString)

  implicit def pathToFilename(p: Path): FileName = new FileName(p)

  implicit def fileToPath(f: File): Path = Path(f.toURI.getPath)

}
