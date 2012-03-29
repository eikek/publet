package org.eknet.publet.engine

import java.util.concurrent.ConcurrentHashMap
import collection.JavaConversions._

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:26
 */
trait EngineRegistry {

  private val engineMap: java.util.Map[Symbol, PubletEngine] = new ConcurrentHashMap[Symbol, PubletEngine]()

  /**
   * Returns the map of registered engines.
   *
   * @return
   */
  def engines: Map[Symbol, PubletEngine] = engineMap.toMap

  def addEngine(engine: PubletEngine) {
    engineMap.put(engine.name, engine)
  }

  def getEngine(name: Symbol): Option[PubletEngine] = Option(engineMap.get(name))

}
