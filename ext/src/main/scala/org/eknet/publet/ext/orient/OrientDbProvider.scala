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

import com.google.inject.{Inject, Singleton}
import org.eknet.publet.web.Config
import java.io.File
import collection.mutable
import actors.Future
import actors.Futures._
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 11.10.12 22:32
 */
trait OrientDbProvider {

  /**
   * Create
   * @param name
   * @return
   */
  def getDatabase(name: String): OrientDb

  /**
   * Converts the database name into an uri that can be used with Orient Databases.
   *
   * @param dbname
   * @return
   */
  def toOrientUri(dbname: String): String

  /**
   * Creates a new [[com.tinkerpop.blueprints.impls.orient.OrientGraph]] instance.
   *
   * @param name
   * @return
   */
  def newGraph(name: String): OrientGraph

  /**
   * Creates a new [[org.eknet.publet.ext.orient.OrientDb]] with a new
   * instance of a [[com.tinkerpop.blueprints.impls.orient.OrientGraph]].
   *
   * @param name
   * @return
   */
  def newDatabase(name: String): OrientDb

}

@Singleton
class DefaultOrientDbProvider @Inject() (config: Config) extends OrientDbProvider {

  private val dbs = new ConcurrentHashMap[String, Future[OrientDb]]()

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

  private def dbroot(config: Config) = {
    val d = new File(config.configDirectory, "databases")
    new File(d, "orient")
  }

  private def databaseDir(config: Config, dbname: String) = new File(dbroot(config), dbname)

  def toOrientUri(dbname: String) = "local://"+ databaseDir(config, dbname).getAbsolutePath

  def newGraph(name: String): OrientGraph = new OrientGraph(toOrientUri(name))

  def newDatabase(name: String): OrientDb = new OrientDb(newGraph(name))

}
