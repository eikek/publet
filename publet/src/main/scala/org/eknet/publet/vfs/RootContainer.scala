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

import scala.Predef._


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.04.12 19:35
 */
trait RootContainer extends Container {
  this: MountManager =>

  private def mountedChildren = resolveMount(Path.root) match {
    case Some(p) => p._2.children
    case None => List()
  }

  def children = mountedChildren ++ tree.children.map(toContainer)

  import ResourceName._

  def content(name: String) = child(name) match {
    case Some(c:ContentResource) => c
    case None => Resource.emptyContent(name.rn)
    case a @ _ => sys.error("Child is not a content resource: "+ a)
  }

  def container(name: String) = child(name) match {
    case Some(c:ContainerResource) => c
    case None => Resource.emptyContainer(name.rn)
    case a @ _ => sys.error("Child is not a container: "+ a)
  }

  def child(name: String) = resolveMount(Path.root) match {
    case Some(t) => t._2.child(name)
    case None => tree.children.find(_.seg == name).map(toContainer)
  }

  override def lookup(path: Path): Option[Resource] = {
    resolveMount(path) match {
      case Some(t) => mountNode(path).map(node=>Some(toContainer(node)))
        .getOrElse(t._2.lookup(path.strip(t._1)))
      case None => if (path == Path.root) Some(toContainer(tree)) else super.lookup(path)
    }
  }

  val exists = true
  lazy val isWriteable = false
  val lastModification = None

  private def toContainer(child:SegTree): ContainerResource = new Inner(child.seg.rn, child)

  class Inner(val name: ResourceName, node: SegTree) extends ContainerResource {

    def children = node.container.map(_.children).getOrElse(node.children.map(toContainer))

    def content(name: String) = {
      val empty = Resource.emptyContent(name.rn)
      if (!isLeaf) empty else {
        node.container.map(_.content(name)).getOrElse(empty)
      }
    }

    def container(name: String) = {
      val empty = Resource.emptyContainer(name.rn)
      (if (!isLeaf) node.children.find(_.seg == name).map(toContainer)
       else node.container.map(_.container(name))).getOrElse(empty)
    }

    def child(name: String) = if (!isLeaf) {
      node.children.find(_.seg == name).map(toContainer)
    } else {
      node.container.flatMap(_.child(name))
    }

    def isLeaf = node.children.isEmpty

    def lastModification = if (!isLeaf) None else {
      node.container match {
        case Some(x) if (x.isInstanceOf[Resource]) => x.asInstanceOf[Resource].lastModification
        case _ => None
      }
    }

    def isWriteable = node.container.exists(_.isWriteable)

    override def toString = "Inner:["+ node.seg +"]"

    val exists = true
  }
}
