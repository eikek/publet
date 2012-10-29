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

package org.eknet.publet.ext.orient

import com.tinkerpop.blueprints._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.10.12 19:26
 */
object GraphDsl {
  import collection.JavaConversions._

  implicit def toVertex(v: RichVertex): Vertex = v.v
  implicit def toRichVertex(v: Vertex): RichVertex = new RichVertex(v)

  implicit def toRichEdge(e: Edge): RichEdge= new RichEdge(e)
  implicit def toEdge(e: RichEdge): Edge = e.e

  /**
   * Creates a new vertex and adds it to the graph.
   * @param graph
   * @return
   */
  def newVertex(init: Vertex => Unit)(implicit graph: BlueprintGraph): Vertex = {
    val v = graph.addVertex()
    init(v)
    v
  }

  /**
   * Creates a new vertex and adds it to the graph.
   * @return
   */
  def newVertex(implicit graph: BlueprintGraph): Vertex = newVertex( v => () )

  val vertexClass = classOf[Vertex]
  val edgeClass = classOf[Edge]

  /**
   * Creates a new vertex with the given key-value property
   * if it does not already exists. The vertex is indexed by
   * the given key.
   *
   * @param key the key for the property
   * @param value the property value
   * @param init optional function to further initialize the new vertex.
   *             this is only used if the vertex does not already
   *             exists and is therefore created.
   * @return
   */
  def vertex(key: String, value: AnyRef, init: Vertex => Unit = v => ())(implicit graph: BlueprintGraph) = {
    import collection.JavaConversions._

    graph.getIndexedKeys(vertexClass).find(_ == key) getOrElse {
      graph.createKeyIndex(key, vertexClass)
    }

    graph.getVertices(key, value).find(v => v.getProperty(key) == value) getOrElse {
      val v = graph.addVertex()
      v.setProperty(key, value)
      init(v)
      v
    }
  }

  def vertices(key: String, value: AnyRef)(implicit db: BlueprintGraph) =
    db.getVertices(key, value).toIterable

  def vertices(implicit db: BlueprintGraph) = db.getVertices.toIterable

  def edges(key: String, value: AnyRef)(implicit db: BlueprintGraph) =
    db.getEdges(key, value).toIterable

  def edges(implicit db: BlueprintGraph) = db.getEdges.toIterable

}

final class EdgeOut(v: Vertex, label: String) {

  /**
   * Creates a new edge to the vertex `o` and adds it to the graph.
   * @param o
   * @param db
   * @return
   */
  def -->(o: Vertex)(implicit db: BlueprintGraph) = db.addEdge(null, v, o, label)

  /**
   * Same as `-->` but returns the other vertex
   * @param o
   * @param db
   * @return
   */
  def -->|(o: Vertex)(implicit db: BlueprintGraph) = { db.addEdge(null, v, o, label); o }

}

final class EdgeIn(v: Vertex, label: String) {
  /**
   * Creates a new edge from the given vertex `o` and adds it to the graph
   * @param o
   * @param db
   * @return
   */
  def <--(o: Vertex)(implicit db: BlueprintGraph) = db.addEdge(null, o, v, label)

  /**
   * Same as `<---` but returns the other vertex.
   *
   * @param o
   * @param db
   * @return
   */
  def <--|(o: Vertex)(implicit db: BlueprintGraph) = { db.addEdge(null, o, v, label); o }

}
final class DynEdge(v: Vertex, dir: Direction, label: String) {

  /**
   * Creates a new edge with direction `dir` and adds
   * it to the graph.
   *
   * @param o
   * @param db
   * @return
   */
  def ---(o: Vertex)(implicit db: BlueprintGraph) = {
    if (dir == Direction.OUT)
      new RichEdge(db.addEdge(null, v, o, label))
    else
      new RichEdge(db.addEdge(null, o, v, label))
  }
}

class RichVertex(val v: Vertex) extends RichElement(v) {

  /**
   * Start creating a new outgoing edge.
   * @param label
   * @return
   */
  def -->(label: String): EdgeOut = new EdgeOut(v, label)

  /**
   * Start creating a new edge with the given direction.
   *
   * @param dir
   * @param label
   * @return
   */
  def ---(dir: Direction, label: String) = new DynEdge(v, dir, label)

  /**
   * Start creating a new incoming edge.
   * @param label
   * @return
   */
  def <--(label: String): EdgeIn = new EdgeIn(v, label)


  /**
   * Iterate over all outgoing edges with the given labels.
   * @param labels
   * @return
   */
  def ->-(labels: String*) = new EdgeIterable(v, Direction.OUT, labels)

  /**
   * Iterate over all incoming edges with the given labels.
   * @param labels
   * @return
   */
  def -<-(labels: String*) =  new EdgeIterable(v, Direction.IN, labels)

  /**
   * Iterate over all edges with the given labels.
   *
   * @param labels
   * @return
   */
  def -<>-(labels: String*) = new EdgeIterable(v, Direction.BOTH, labels)

  /**
   * Iterate over all adjecent vertices.
   *
   * @param labels
   * @return
   */
  def adjacents(labels: String*) =
    collection.JavaConversions.iterableAsScalaIterable(v.getVertices(Direction.BOTH, labels: _*))

  /**
   * Iterate over all edges.
   *
   * @param labels
   * @return
   */
  def edges(labels: String*) =
    collection.JavaConversions.iterableAsScalaIterable(v.getEdges(Direction.BOTH, labels: _*))
}

class RichEdge(val e: Edge) extends RichElement(e) {
  def label = e.getLabel
  def inVertex = e.getVertex(Direction.IN)
  def outVertex = e.getVertex(Direction.OUT)
  def other(v: Vertex) = v match {
    case x if (x == inVertex) => outVertex
    case x if (x == outVertex) => inVertex
    case _ => sys.error("Given vertex is not part of this edge: "+ v)
  }
}
class RichElement(el: Element) {
  import collection.JavaConversions._

  def get[A](key:String) = el.getProperty(key).asInstanceOf[A]
  def apply(key: String) = el.getProperty(key)
  def update[A](key: String, value: A): this.type = { el.setProperty(key, value); this }
  def +=(t: (String, Any)): this.type = { update(t._1, t._2); this }
  def -=(key: String): this.type = { el.removeProperty(key); this }
  def keySet = el.getPropertyKeys.toSet
}

class EdgeIterable(v: Vertex, dir: Direction, labels: Seq[String]) extends Iterable[Edge] {
  import collection.JavaConversions._

  def iterator = v.getEdges(dir, labels: _*).toIterator

  /**
   * Iterate over adjencent vertices.
   * @return
   */
  def ends = v.getVertices(dir, labels: _*).toIterable

  //shortcuts
  def findEnd(p: Vertex => Boolean) = ends find p
  def filterEnds(p: Vertex => Boolean) = ends filter p
  def foreachEnd[A](f: Vertex => A) { ends foreach f }
  def mapEnds[A](f: Vertex => A) = ends map f

}