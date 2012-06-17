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

package org.eknet.publet.engine

import org.eknet.publet.vfs.Path
import org.eknet.publet.Glob
import collection.mutable

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 13:21
 */
trait EngineMangager {

  private val mountMap = mutable.Map[Glob, PubletEngine]()
  private val engineMap = mutable.Map[Symbol, PubletEngine]()

  /**
   * Returns the map of registered engines.
   *
   * @return
   */
  def engines: Map[Symbol, PubletEngine] = engineMap.toMap

  /**
   * Adds the engine without registering a url pattern
   * @param engine
   */
  def addEngine(engine: PubletEngine) {
    engineMap.put(engine.name, engine)
  }

  /**
   * Returns an engine by name.
   *
   * @param name
   * @return
   */
  def getEngine(name: Symbol): Option[PubletEngine] = engineMap.get(name)

  /**
   * Adds the given engine and registers it to the specified
   * url pattern.
   *
   * @param urlPattern
   * @param engine
   */
  def register(urlPattern: String, engine: PubletEngine) {
    val glob = Glob(urlPattern)
    val e = getEngine(engine.name).getOrElse({
      addEngine(engine)
      engine
    })
    mountMap.put(glob, e)
  }

  def resolveEngine(path: Path): Option[PubletEngine] = {
    def keyget(keys: List[Glob]): Option[PubletEngine] =  (keys: @unchecked) match {
      case Nil => None
      case a :: tail if a.matches(path.asString) => mountMap.get(a) match {
        case None =>  { keyget(tail) }
        case r @ Some(x) => r
      }
      case a :: tail if !a.matches(path.asString) => keyget(tail)
    }
    def sortedKeys = {
      val longestFirst = (x: Glob, y: Glob) => x.compare(y) > 0
      mountMap.keys.toList.sortWith(longestFirst)
    }
    keyget(sortedKeys)
  }

}
