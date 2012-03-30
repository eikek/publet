package org.eknet.publet.engine

import scala.collection.JavaConversions._


import java.util.concurrent.ConcurrentHashMap
import org.eknet.publet.Uri
import org.eknet.publet.impl.Glob

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 13:21
 */
trait EngineResolver {
  this: EngineRegistry =>

  private val mountMap = new ConcurrentHashMap[Glob, PubletEngine]() 

  private def get(k: Glob): Option[PubletEngine] = Option(mountMap.get(k))

  private def sortedKeys = {
    val longestFirst = (x: Glob, y: Glob) => x.compare(y) > 0
    mountMap.keySet().toList.sortWith(longestFirst)
  }

  def mount(urlPattern: String, engine: PubletEngine) {
    val glob = Glob(urlPattern)
    addEngine(engine)
    mountMap.put(glob, engine)
  }

  protected[publet] def resolveEngine(uri: Uri): Option[PubletEngine] = {
    def keyget(keys: List[Glob]): Option[PubletEngine] =  (keys: @unchecked) match {
      case Nil => None
      case a :: tail if a.matches(uri) => get(a) match {
        case None =>  { println(a); println(tail); keyget(tail) }
        case r @ Some(x) => r
      }
      case a :: tail if !a.matches(uri) => keyget(tail)
    }
    uri.parameter("source") match {
      case None => keyget(sortedKeys)
      case Some(_) => getEngine(Symbol("source"))
    }
  }

}
