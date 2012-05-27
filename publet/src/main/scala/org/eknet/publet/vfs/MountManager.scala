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

import collection.mutable
import org.eknet.publet.impl.Conversions._

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 02.04.12 23:01
 */
trait MountManager {

  protected val tree = SegTree("/")

  def mount(path: Path, part: Container) {
    Predef.ensuring(path != null, "null")
    Predef.ensuring(part != null, "null")
    if (mountNode(path).isDefined) throwException("Path already mounted")

    if (!path.isRoot) tree.add(path, part)
  }

  def mountNode(path: Path) = tree.get(path)

  def resolveMount(path: Path): Option[(Path, Container)] = {
    val seg = tree.findSeg(path)
    seg._2.container.map(Path(seg._1, true, true)->_)
  }

  case class SegTree(seg:String, children: mutable.ListBuffer[SegTree] = mutable.ListBuffer()) {

    var container: Option[Container] = None

    def add(path:Path, cont: Container) {
      path.segments match {
        case a :: Nil => children.find(_.seg == a).getOrElse(addChild(a)).container = Some(cont)
        case a :: xs => children.find(_.seg == a).getOrElse(addChild(a)).add(path.tail, cont)
        case Nil =>
      }
    }

    def addChild(seg:String):SegTree = {
      val node = SegTree(seg)
      children.append(node)
      node
    }

    /** Returns the node at the given path*/
    def get(path:Path): Option[SegTree] = {
      path.segments match {
        case a :: as ::at => children.find(_.seg == a).flatMap(_.get(path.tail))
        case a :: Nil => children.find(_.seg == a)
        case Nil => None
      }
    }

    /**Returns the node with the longest path matching the given path together with
     * the path of this node
     */
    def findSeg(path:Path): (List[String], SegTree) = {
      def segFind(path: Path, node: SegTree, coll: List[String]): (List[String], SegTree) = {
        path.segments match {
          case a::as => node.children.find(_.seg == a).map(c=>segFind(path.tail, c, a::coll)).getOrElse((coll.reverse->node))
          case Nil => coll->node
        }
      }
      segFind(path, this, List())
    }
  }
}
