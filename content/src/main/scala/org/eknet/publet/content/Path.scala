package org.eknet.publet.content

import java.nio.file.{Path => JPath, Files, Paths}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.05.13 19:25
 */
sealed trait Path {
  def head: String
  def tail: Path
  def / (other: Path): Path
  def /::(other: Path) = this / other
  def prepend(other: Path) = /::(other)
  def /:(seg: String) = new /(seg, this)
  def drop(n: Int): Path
  def take(n: Int): Path
  def size: Int
  def segments: List[String]
  def isEmpty: Boolean
  def parent: Path
  def startsWith(other: Path): Boolean
  def mkString(sep: String): String

  /**
   * The last segment of this path wrapped
   * in a `Name`.
   *
   * @return
   */
  def fileName: Name
  def absoluteString: String = "/" + mkString("/")
  def reverse: Path = Path(segments.reverse)
  override def toString = mkString("/")
}

final case class / (head: String, tail: Path) extends Path {
  val isEmpty = false
  lazy val size = 1 + tail.size
  lazy val segments = head :: tail.segments
  lazy val fileName = tail match {
    case EmptyPath => Name(head)
    case _ => tail.fileName
  }
  def /(other: Path) = new /(head, tail / other)
  def drop(n: Int) = if (n==0) this else tail.drop(n-1)
  def take(n: Int) = if (n==0) EmptyPath else new /(head, tail.take(n-1))
  def startsWith(other: Path) = if (size < other.size) false else {
    take(other.size) == other
  }
  def parent = if (tail.size == 0) EmptyPath else new /(head, tail.parent)

  def mkString(sep: String) = segments.mkString(sep)
}

case object EmptyPath extends Path {
  private def noElementError = throw new NoSuchElementException
  def head = noElementError
  def tail = noElementError
  def drop(n: Int) = if (n==0) this else noElementError
  def take(n: Int) = if (n==0) this else noElementError
  def fileName = noElementError
  def parent = noElementError

  def isEmpty = true
  def /(other: Path) = other
  def size = 0
  def segments = Nil
  def startsWith(other: Path) = other == this
  def mkString(sep: String) = ""
}

object Path {

  val root = EmptyPath

  implicit def apply(str: String): Path = if (str.isEmpty) EmptyPath else apply(Paths.get(str))

  implicit def apply(p: JPath): Path = {
    import collection.JavaConversions._
    val segs = p.normalize().map(_.toString).toList
    apply(segs)
  }

  implicit def apply(name: Name): Path = Path(name.fullName)

  def apply(segments: List[String]): Path = segments match {
    case Nil => EmptyPath
    case a::as => new /(a, apply(as))
  }
}