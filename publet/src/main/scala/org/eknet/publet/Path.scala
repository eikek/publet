package org.eknet.publet

import tools.nsc.io.{Directory, File}
import collection.immutable.List


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
  
  lazy val asString = (if (absolute) "/" else "") + segments.mkString(String.valueOf(Path.sep))
  
  lazy val size = segments.length
  
  def prefixedBy(p: Path): Boolean = {
    if (p.size > size) false
    else segments.slice(0, p.size) == p.segments
  }  
  
  def toAbsolute = if (absolute) this else Path(segments, true)
  
  def toRelative = if (!absolute) this else Path(segments, false)

  def / (p: Path) = Path(segments ++ p.segments, absolute)

  def / (p: String) = Path(segments ++ Path(p).segments, absolute)

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
  
  def apply(d: Directory): Path = Path(d.toURI.getPath)
  
  implicit def toFile(p: Path): File = File(p.asString)

  implicit def toDirectory(p: Path): Directory = Directory(p.asString)

  implicit def pathToFilename(p: Path): FileName = new FileName(p)

}
