package org.eknet.publet.engine

import scala.collection._
import org.eknet.publet.Path

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 13:21
 */
trait EngineResolver {

  private val mountMap = mutable.Map[Glob, PubletEngine]()
  private val engineMap = mutable.Map[Symbol, PubletEngine]()

  /**
   * Returns the map of registered engines.
   *
   * @return
   */
  def engines: Map[Symbol, PubletEngine] = engineMap.toMap

  def addEngine(engine: PubletEngine) {
    engineMap.put(engine.name, engine)
  }

  def getEngine(name: Symbol): Option[PubletEngine] = engineMap.get(name)

  private def get(k: Glob): Option[PubletEngine] = mountMap.get(k)

  private def sortedKeys = {
    val longestFirst = (x: Glob, y: Glob) => x.compare(y) > 0
    mountMap.keys.toList.sortWith(longestFirst)
  }

  def register(urlPattern: String, engine: PubletEngine) {
    val glob = Glob(urlPattern)
    addEngine(engine)
    mountMap.put(glob, engine)
  }

  protected[publet] def resolveEngine(path: Path): Option[PubletEngine] = {
    def keyget(keys: List[Glob]): Option[PubletEngine] =  (keys: @unchecked) match {
      case Nil => None
      case a :: tail if a.matches(path.asString) => get(a) match {
        case None =>  { println(a); println(tail); keyget(tail) }
        case r @ Some(x) => r
      }
      case a :: tail if !a.matches(path.asString) => keyget(tail)
    }
    keyget(sortedKeys)
  }

}
