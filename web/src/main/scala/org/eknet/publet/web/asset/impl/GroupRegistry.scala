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
import collection.mutable.ListBuffer
import org.eknet.publet.web.asset.{Kind, Group}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.09.12 01:32
 */
class GroupRegistry {
  private val graph = collection.mutable.Map[String, Node]()

  def setup(groups: Group*) {
    //find group in tree or create new node
    for (group <- groups) {
      val node = graph.get(group.name).map(_.merge(group)) getOrElse Node(group)
      graph.put(group.name, node)
    }
  }

  /**
   * Returns all sources registered with the given group in the
   * correct order.
   *
   * @param group
   * @return
   */
  def getSources(group: String, path: Path, kind: Kind.KindVal) = {
    val root = graph.get(group).getOrElse(sys.error("Asset group '"+ group+"' not registered."))
    def collect(nodes: Set[Node]): List[Set[Node]] = {
      if (nodes.exists(!_.group.uses.isEmpty)) {
        val x = nodes.flatMap(n => n.group.uses).map(s => {
          graph.get(s).getOrElse(sys.error("Asset group '"+ s +"' not registered."))
        })
        x :: collect(x)
      } else {
        List(nodes)
      }
    }
    //collect minimum node list
    val nodeList = collect(Set(root)).flatten
      .distinct
      .filter(_.group.pathPattern.matches(path.asString))

    //create graph for sorting
    val sorted = new Graph(nodeList).topoSort

    // topo-sort to resource-list
    sorted.flatMap(_.group.resources.reverse.filter(_.name.ext == kind.ext))
  }

  private class Graph(nodeList: Seq[Node]) {
    val nodes = collection.mutable.Set() ++ (nodeList)
    val edges = collection.mutable.Set[Edge]()
    nodeList.map { node =>
      for (after <- node.group.afters) {
        val an = graph.get(after).getOrElse(sys.error("Group '"+after+"' not in graph"))
        edges += Edge(an, node)
        nodes += an
      }
      for (before <- node.group.befores) {
        val bn = graph.get(before).getOrElse(sys.error("Group '"+before+"' not in graph"))
        edges += Edge(node, bn)
        nodes += bn
      }
    }

    def nextNode = nodes.find(n => !edges.map(_.end).contains(n))

    def topoSort: List[Node] = {
      val buf = new ListBuffer[Node]
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
