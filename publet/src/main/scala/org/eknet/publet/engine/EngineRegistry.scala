package org.eknet.publet.engine

import util.matching.Regex
import java.util.concurrent.ConcurrentHashMap
import collection.JavaConversions._

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:26
 */
trait EngineRegistry {

  private val engineMap = new ConcurrentHashMap[Symbol, Engine]()

  type Engine = (Regex, PubletEngine)

  /**
   * Returns the map of registered engines.
   *
   * @return
   */
  def engines: Map[Symbol, Engine] = engineMap.toMap

  def addEngine(urlPattern: Regex, engine: PubletEngine) {
    engineMap.put(engine.name, (urlPattern, engine))
  }

  def getEngine(name: Symbol): Option[(Regex, PubletEngine)] = Option(engineMap.get(name))

  protected[publet] def registeredEngines: List[Engine] = engineMap.values().toList

}
