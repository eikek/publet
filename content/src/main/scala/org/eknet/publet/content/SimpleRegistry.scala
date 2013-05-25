package org.eknet.publet.content

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.05.13 18:52
 */
abstract class SimpleRegistry[K, V](map: Map[K, List[V]] = Map.empty) {

  protected def addObject(key: K, value: V) = {
    val next = value :: map.get(key).getOrElse(Nil)
    map.updated(key, next)
  }

  protected def removeObject(key: K, value: V) = {
    val next = map.get(key).map(list => list.filter(_ != value))
    map.updated(key, next)
  }

  protected def removeKey(key: K) = map - key


  protected def get(key: K) = map.get(key)

  protected def keySet = map.keySet

  protected def headOption(key: K) = map.get(key).flatMap(_.headOption)
}
