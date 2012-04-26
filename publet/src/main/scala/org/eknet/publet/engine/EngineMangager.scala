package org.eknet.publet.engine

import scala.collection._
import org.eknet.publet.vfs.Path

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
