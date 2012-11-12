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

package org.eknet.publet.ext.graphdb

import com.google.inject.{Inject, Singleton}
import org.eknet.publet.web.Config
import java.io.File
import scala.actors.{Futures, Future}
import actors.Futures._
import java.util.concurrent.ConcurrentHashMap
import com.thinkaurelius.titan.core.{TitanGraph, TitanFactory}
import com.tinkerpop.blueprints.{Vertex, Edge, Element}
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 11.10.12 22:32
 */
trait GraphDbProvider {

  /**
   * Creates a new database or opens an existing one. Subsequent calls with
   * the same argument may yield in returning the same object.
   *
   * @param name
   * @return
   */
  def getDatabase(name: String): GraphDb

  /**
   * Creates a new [[org.eknet.publet.ext.graphdb.BlueprintGraph]] instance.
   *
   * @param name
   * @return
   */
  def newGraph(name: String): BlueprintGraph

  /**
   * Creates a new [[org.eknet.publet.ext.graphdb.GraphDb]] using
   * a new [[org.eknet.publet.ext.graphdb.BlueprintGraph]] instance.
   *
   *
   * @param name
   * @return
   */
  def newDatabase(name: String): GraphDb

  /**
   * Shutdown of all databases created so far.
   *
   */
  def shutdownAll()

}

@Singleton
class DefaultGraphDbProvider @Inject() (config: Config) extends GraphDbProvider {

  private val dbs = new ConcurrentHashMap[String, Future[GraphDb]]()

  /**
   * Returns a database for the given name. If it already exists, the same instance
   * is returned.
   *
   * @param name
   */
  def getDatabase(name: String) = {
    val newTask = future {
      newDatabase(name)
    }
    val task = dbs.putIfAbsent(name, newTask)
    if (task == null) {
      newTask()
    } else {
      task()
    }
  }

  def shutdownAll() {
    import collection.JavaConversions._

    for (future <- dbs.values()) {
      if (future.isSet) {
        future().shutdown()
      } //todo: otherwise cancel
    }
  }

  private def dbroot(config: Config) = {
    val d = new File(config.configDirectory, "databases")
    new File(d, "titans")
  }

  private def databaseDir(config: Config, dbname: String) = new File(dbroot(config), dbname)

  def toOrientUri(dbname: String) = "local://"+ databaseDir(config, dbname).getAbsolutePath

  def newGraph(name: String): BlueprintGraph = wrapTitanGraph(TitanFactory.open(databaseDir(config, name).getAbsolutePath))

  def newDatabase(name: String): GraphDb = new GraphDb(newGraph(name))

  private def wrapTitanGraph(tg: TitanGraph): BlueprintGraph = new TitanWrapper(tg)

  private class TitanWrapper(titan: TitanGraph) extends BlueprintGraph {
    def getFeatures = titan.getFeatures
    def addVertex(id: Any) = titan.addVertex(id)
    def getVertex(id: Any) = titan.getVertex(id)
    def removeVertex(vertex: Vertex) { titan.removeVertex(vertex) }
    def getVertices = titan.getVertices
    def getVertices(key: String, value: Any) = titan.getVertices(key, value)
    def addEdge(id: Any, outVertex: Vertex, inVertex: Vertex, label: String) = titan.addEdge(id, outVertex, inVertex, label)
    def getEdge(id: Any) = titan.getEdge(id)
    def removeEdge(edge: Edge) { titan.removeEdge(edge) }
    def getEdges = titan.getEdges
    def getEdges(key: String, value: Any) = titan.getEdges(key, value)
    def dropKeyIndex[T <: Element](key: String, elementClass: Class[T]) { titan.dropKeyIndex(key, elementClass) }
    def createKeyIndex[T <: Element](key: String, elementClass: Class[T]) { titan.createKeyIndex(key, elementClass) }
    def getIndexedKeys[T <: Element](elementClass: Class[T]) = titan.getIndexedKeys(elementClass)
    def stopTransaction(conclusion: Conclusion) { titan.stopTransaction(conclusion) }
    def shutdown() { titan.shutdown() }
  }
}
