/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.publet.vfs

import collection.immutable.List
import java.io.File
import org.eknet.publet.vfs.Resource._
import java.net.URLEncoder

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 30.03.12 18:41
 */
case class Path(segments: List[String], query: Option[String], absolute: Boolean, directory: Boolean) extends Ordered[Path] {

  /**
   * Returns a copy of this without the last element
   */
  lazy val parent = if (isRoot) this else Path(segments.take(segments.length - 1), query, absolute, true)

  def strip = Path(segments.tail, query, absolute, directory)

  def head = segments.head
  def tail = Path(segments.tail, query, false, directory)

  def strip(p: Path): Path = {
    if (prefixedBy(p)) Path(segments.slice(p.size, size), query, absolute, directory)
    else this
  }
  def strip(n: Int): Path = {
    Path(segments.drop(n), query, false, directory)
  }

  lazy val isRoot = segments.isEmpty

  def child(name: String) = {
    val directory = name.endsWith(String.valueOf(Path.sep))
    val cn = if (directory) name.substring(0, name.length-1) else name
    Path(segments ::: List(cn), query, absolute, directory)
  }

  def sibling(name: String) = parent.child(name)

  private def toString(segF: String=>String) = {
    val buf = new StringBuilder()
    if (absolute) {
      buf.append(String.valueOf(Path.sep))
    }
    buf.append(segments.map(segF).mkString(String.valueOf(Path.sep)))
    if (directory && !segments.isEmpty) {
      buf.append(String.valueOf(Path.sep))
    }
    buf.toString()
  }

  lazy val asString = toString(identity)

  /**
   * Returns this path as a url-encoded string.
   */
  lazy val asUrlString =
    toString(URLEncoder.encode(_, "UTF-8")) + query.map(q => "?"+q).getOrElse("")

  lazy val size = segments.length

  /**
   * The last element as `ResourceName`
   *
   */
  lazy val name = if (isRoot) ResourceName("/") else ResourceName(segments.last)

  /**
   * A concatenation of '../' up to the root
   *
   */
  lazy val relativeRoot = if (directory) ("../" * size) else ("../" * parent.size)

  /**
   * Checks if this path is a descendend of the given path.
   *
   * That is, the given path is of lower or equal size and
   * its common segments match.
   *
   * `Path("/a/b/c").prefixedBy(Path("/a/b")` is `true`,
   * for example.
   *
   * @param p
   * @return
   */
  def prefixedBy(p: Path): Boolean = {
    if (p.size > size) false
    else segments.slice(0, p.size) == p.segments
  }

  def toAbsolute = if (absolute) this else Path(segments, query, true, directory)

  def toRelative = if (!absolute) this else Path(segments, query, false, directory)

  def /(p: Path): Path = Path(segments ++ p.segments, query, absolute, p.directory)

  def /(s: String): Path = this / Path(s)

  def /(rn: ResourceName):Path = this / Path(rn.fullName)

  def /(r: Resource): Path = Path(this.segments++ Path(r.name.fullName).segments, query, absolute, isContainer(r))

  def toFile(p: Path): File = new File(p.asString)

  def compare(that: Path) = that.size - size

  def withExt(ext: String) = parent / name.withExtension(ext)

  /**
   * Rebases the given path against this, such that this path
   * is reachable from the given path.
   *
   *{{{
   *   /main/proj/.includes/theme.css  = this
   *   /main/proj/test/mop/index.html = other
   *   --> ../../.includes/theme.css  = result
   *}}}
   * or
   *{{{
   *   /main/proj/.includes/theme.css = ths
   *   /main/proj/index.html   = other
   *   --> ../.includes/theme.css = result
   *}}}
   * or
   *{{{
   *   /main/proj/theme/test.css = this
   *   /main/proj/index.html = other
   *   --> theme/test.css
   *}}}
   *
   * @param other
   * @return
   */
  def rebase(other: Path): Path = {
    //find parent
    def findParentOff(p0: Path, p1: Path): Int = {
      if (p0.size>0 && p1.size>0) {
        if (p0.head == p1.head) 1 + findParentOff(p0.strip, p1.strip)
        else 0
      } else 0
    }
    val parentOffset = findParentOff(this, other)
    val pt = this.strip(parentOffset)
    val po = other.strip(parentOffset)

    //combine
    Path("../" * (other.size - po.size)) / pt
  }
}

object Path {

  val root = Path(List(), None, true, true)

  private val sep = '/'

  def apply(str: String): Path = {
    Predef.ensuring(str.length() > 0, "empty paths are not allowed. use Path.root.")
    if (str == "/") root
    else {
      val pathRegex = "((.*?)(\\?.*)?)".r
      str match {
        case pathRegex(f, p, q) => {
          val segs = p.split(sep)
          val abs = str.charAt(0) == sep
          val dir = str.endsWith(String.valueOf(Path.sep))
          if (abs) new Path(segs.tail.toList, Option(q).map(_.substring(1)), abs, dir)
          else new Path(segs.toList, Option(q).map(_.substring(1)), abs, dir)
        }
        case _ => sys.error("Wrong path: "+str)
      }
    }
  }

  def apply(segments: List[String], absolute: Boolean, directory: Boolean): Path = new Path(segments, None, absolute, directory)

  def apply(f: File): Path = Path(f.toURI.getPath)

  implicit def fileToPath(f: File): Path = Path(f)

  class StringPath(str: String) {
    def p = Path(str)
  }
  implicit def stringToPath(str: String) = new StringPath(str)
}
