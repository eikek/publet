package org.eknet.publet.actor

import scala.collection.Map

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 08.05.13 22:55
 */
object utils {

  object Stopwatch {
    def start(): () => Long = {
      val start = System.currentTimeMillis()
      () => System.currentTimeMillis() - start
    }
  }

  def toposortLayers[T](tree: Map[T, Set[T]]): Either[Map[T, Set[T]], List[List[T]]] = {

    def recurse(tree: Map[T, Set[T]]): Either[Map[T, Set[T]], List[List[T]]] = {
      tree.filter(p => p._2.isEmpty) match {
        case set if (set.isEmpty) => if (tree.isEmpty) Right(Nil) else Left(tree)
        case set => {
          val next = for ((k, ch) <- tree if (!set.contains(k))) yield (k, ch.filterNot(c => set.contains(c)))
          recurse(next).right.map(tail => set.keys.toList :: tail)
        }
      }
    }
    recurse(tree)
  }
}
