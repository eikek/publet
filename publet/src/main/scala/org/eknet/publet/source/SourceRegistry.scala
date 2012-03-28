package org.eknet.publet.source

import java.util.concurrent.ConcurrentHashMap
import collection.JavaConversions._

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:27
 */
trait SourceRegistry {

  private val sourceMap = new ConcurrentHashMap[Symbol, PubletSource]()

  /**
   * Returns the map of registered source.
   *
   * @return
   */
  def sources: Map[Symbol, PubletSource] = sourceMap.toMap

  def addSource(source: PubletSource) {
    sourceMap.put(source.name, source)
  }

  def getSource(name: Symbol): PubletSource = sourceMap.get(name) match {
    case null => sys.error("No source registered: "+ name.name)
    case s => s
  }
}
