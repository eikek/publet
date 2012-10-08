
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

package org.eknet.publet.web.asset.impl

import org.eknet.publet.vfs.Path
import org.eknet.publet.Glob
import org.eknet.publet.web.asset.{AssetResource, Kind, Group}
import collection.mutable
import org.eknet.publet.web.{RunMode, Config}
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.09.12 01:32
 */
class GroupRegistry extends Logging {
  private val graph = collection.mutable.Map[String, Node]()

  def setup(groups: Group*) {
    //find group in tree or create new node
    for (group <- groups) {
      val node = graph.get(group.name).map(_.merge(group)) getOrElse Node(group)
      graph.put(group.name, node)
    }
  }

  def getGroups = graph.values.map(_.group).toList

  def getSourcesUnordered = graph.values.flatMap(_.group.resources)

  /**
   * Returns all resources of types not listed in [[org.eknet.publet.web.asset.Kind]]
   * for the specified group.
   *
   * @param group
   * @param path
   * @return
   */
  def getUnknownResources(group: Iterable[String], path: Option[Path]) =
    collectSources(group, path, f => !Kind.values.toSet.contains(f.name.ext))

  /**
   * Returns all sources registered with the given group in the
   * correct order.
   *
   * @param group
   * @return
   */
  def getSources(group: Iterable[String], path: Option[Path], kind: Kind.KindVal) =
    collectSources(group, path, f => f.name.ext == kind.ext)

  private def groupNotFound[A](ret: A, names: String*): A = {
    val msg = "Asset group '"+names.mkString(", ")+"' not registered"
    if (Config.mode == RunMode.development)
      throw new IllegalStateException(msg)
    else {
      error(msg)
      ret
    }
  }

  private def collectSources(groups: Iterable[String], path: Option[Path], predicate: AssetResource => Boolean) = {
    val roots = groups.map(group => graph.get(group).getOrElse(groupNotFound(Node(Group("_")), group)))
    def collect(nodes: Set[Node]): List[Set[Node]] = {
      if (nodes.exists(!_.group.uses.isEmpty)) {
        val x = nodes.flatMap(n => n.group.uses).map(s => {
          graph.get(s).getOrElse(groupNotFound(Node(Group("_")), s))
        })
        x :: collect(x)
      } else {
        List(nodes)
      }
    }
    //collect minimum node list
    val nodeList = path match {
      case Some(p) => (roots.toList ::: collect(roots.toSet).flatten)
        .distinct
        .filter(_.group.pathPattern.matches(p.asString))
      case _ => (roots.toList ::: collect(roots.toSet).flatten)
        .distinct
    }

    //create graph for sorting
    val sorted = new Graph(nodeList).topoSort

    // topo-sort to resource-list
    sorted.flatMap(_.group.resources.reverse.filter(predicate))
  }

  private class Graph(nodeList: Seq[Node]) {
    val nodes = collection.mutable.Set() ++ (nodeList)
    val edges = collection.mutable.Set[Edge]()
    fillGraph(nodeList.toList)

    def fillGraph(nodes: List[Node]) {
      nodes match {
        case node::ns => {
          val reqnodes = mutable.Set[Node]()
          for (after <- node.group.afters) {
            val an = graph.get(after).getOrElse(groupNotFound(Node(Group("_")), after))
            edges += Edge(an, node)
            reqnodes += an
          }
          for (before <- node.group.befores) {
            val bn = graph.get(before).getOrElse(groupNotFound(Node(Group("_")), before))
            edges += Edge(node, bn)
            reqnodes += bn
          }
          this.nodes ++= reqnodes
          fillGraph( ns ::: reqnodes.toList )
        }
        case Nil =>
      }
    }

    def nextNode = nodes.find(n => !edges.map(_.end).contains(n))

    def topoSort: List[Node] = {
      val buf = new mutable.ListBuffer[Node]
      while (!nodes.isEmpty) {
        nextNode map { n =>
          buf.append(n)
          nodes.remove(n)
          edges.filter(e => e.start == n || e.end == n).map(edges.remove)
        } getOrElse(sys.error("circular dependencies in groups"))
      }
      buf.toList
    }
  }

  private case class Edge(start: Node, end: Node)
  private case class Node(group: Group) {

    def merge(other: Group): Node = {
      if (other.pathPattern != Glob("**")
        && other.pathPattern != group.pathPattern) {
        throw new IllegalArgumentException("There already exists a group '"+group.name+"' with a different path")
      }

      Node(Group(group.name,
        group.pathPattern,
        other.resources ::: group.resources,
        other.befores ++ group.befores,
        other.afters ++ group.afters,
        other.uses ++ group.uses))
    }
  }
}
