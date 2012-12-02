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
import java.io.{BufferedOutputStream, FileOutputStream, File}
import scala.actors.Future
import actors.Futures._
import java.util.concurrent.ConcurrentHashMap
import com.tinkerpop.blueprints.{Vertex, Edge, Element}
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import com.orientechnologies.orient.core.db.graph.OGraphDatabase
import com.orientechnologies.orient.core.db.ODatabase
import com.orientechnologies.orient.core.config.OGlobalConfiguration
import org.eknet.scue.util.{ForwardingGraph, ScueIdGraph}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 11.10.12 22:32
 */
trait GraphDbProvider {

  /**
   * Creates a new database or opens an existing one. Subsequent calls with
   * the same argument yield in returning the same object where the `newXyz`
   * methods return new objects potentially accessing the same database.
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
   * @param name
   * @return
   */
  def newDatabase(name: String): GraphDb

  /**
   * Shutdown of all databases created so far.
   *
   */
  def shutdownAll()

  /**
   * Returns a list of the names of all currently registered databases.
   *
   * @return
   */
  def registeredDatabases: Iterable[String]
}

@Singleton
class DefaultGraphDbProvider @Inject() (config: Config) extends GraphDbProvider with GraphDbProviderMBean {

  private val dbs = new ConcurrentHashMap[String, Future[GraphDb]]()

  //disable l1 cache. See http://tinkerpop.com/docs/wikidocs/blueprints/2.1.0/OrientDB-Implementation.html
  OGlobalConfiguration.CACHE_LEVEL1_ENABLED.setValue(false)

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

  def registeredDatabases: Iterable[String] = {
    import collection.JavaConversions._
    dbs.keySet().toSet
  }

  private[this] def dbroot(config: Config) = {
    val d = new File(config.configDirectory, "databases")
    new File(d, "orientdbs")
  }

  private[this] def databaseDir(config: Config, dbname: String) = new File(dbroot(config), dbname)

  def newGraph(name: String): BlueprintGraph = {
    val og = new OrientGraph("local:"+ databaseDir(config, name).getAbsolutePath) with BlueprintGraph
    og.getRawGraph.setLockMode(OGraphDatabase.LOCK_MODE.DATABASE_LEVEL_LOCKING)
    og
  }

  def newDatabase(name: String): GraphDb = new GraphDb(newGraph(name))

  def getDatabases = registeredDatabases.toArray

  def exportDatabase(name: String) {
    if (!dbs.keySet().contains(name)) {
      throw new IllegalArgumentException("Database '"+name+"' does not exist.")
    }
    val db = getDatabase(name)
    val file = config.getFile(name+"_"+System.currentTimeMillis()+".ml")
    val fout = new BufferedOutputStream(new FileOutputStream(file))
    db.exportToGraphML(fout)
    fout.flush()
    fout.close()
  }
}
