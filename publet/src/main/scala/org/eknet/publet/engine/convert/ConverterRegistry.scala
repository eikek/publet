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

package org.eknet.publet.engine.convert

import collection._
import mutable.ListBuffer
import org.eknet.publet.vfs.{Path, ContentType, Content}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 12:43
 */
trait ConverterRegistry {
  private val graph = new Graph

  def addConverter(mapping: (ContentType, ContentType), c: ConverterEngine#Converter) {
    graph.add(mapping, c)
  }

  protected[engine] def converterFor(source: ContentType, target: ContentType): Option[ConverterEngine#Converter] = {
    if (source == target) Some(graph.idconv)
    else graph.converterChain(source, target)
  }

  private class Graph {

    type Converter = ConverterEngine#Converter

    val idconv: Converter = (p: Path, x: Content) => x
    private val converters = mutable.Map[(ContentType, ContentType), Converter]()
    private val nodes = mutable.Map[ContentType, List[ContentType]]()

    def add(mapping: (ContentType, ContentType), c: Converter) {
      converters.put(mapping, c)
      val list = nodes.get(mapping._1).getOrElse(List())
      nodes.put(mapping._1, mapping._2 :: list)
      nodes.put(mapping._2, List())
    }

    def converterChain(s: ContentType, e: ContentType): Option[Converter] = {
      //Dijkstra
      val distance = mutable.Map[ContentType, Int]()
      val predecessor = mutable.Map[ContentType, ContentType]()
      val nodemap = mutable.Map() ++ nodes
      distance.put(s, 0)
      while (!nodemap.isEmpty && !distance.isEmpty) {
        val next = distance.toSeq.sortBy(_._2).head._1
        if (next != e) {
          nodemap.remove(next)
          nodes.get(next).get.foreach(ct => {
            if (nodemap.contains(ct)) {
              val a = distance.get(next).get + 1
              if (a < distance.get(ct).getOrElse(Int.MaxValue)) {
                distance.put(ct, a)
                predecessor.put(ct, next)
              }
            }
          })
          distance.remove(next)
        } else {
          nodemap.clear()
        }
      }
      //first create shortest path
      val path = mutable.ListBuffer[ContentType](e)
      var z = e
      while (predecessor.get(z).isDefined) {
        z = predecessor.get(z).get
        path.prepend(z)
      }
      //create tuples that define the converter
      path zip path.tail match {
        case ListBuffer() => None
        //create function by composing all converters c1(p, c2(p,c))
        case tuples => Some(tuples.foldLeft(idconv)((c, t) => ConverterEngine.compose(c, converters.get(t).get)))
      }
    }
  }

}
