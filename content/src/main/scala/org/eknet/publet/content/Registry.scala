package org.eknet.publet.content

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 11.05.13 01:16
 */
trait Registry {

  type Key
  type Value

  private val mappings = new AtomicReference(Map.empty[Key, List[Value]])

  final def register(key: Key, value: Value) {
    val cb = () => onRegister(key, value)
    modify(cb, map => {
      val list = map.get(key).map(list => value :: list).getOrElse(List(value))
      map.updated(key, list)
    })
  }

  final def unregister(key: Key) = {
    val cb = () => onUnregister(key)
    modify(cb, map => {
      val list = map.get(key).filterNot(_.isEmpty)
        .getOrElse(sys.error(s"Path $key not registered."))

      list.tail match {
        case Nil => map - key
        case tail => map.updated(key, tail)
      }
    })
  }

  protected def onUnregister(key: Key) {}
  protected def onRegister(key: Key, value: Value) {}

  @tailrec
  private[this] def modify(cb: () => Unit, f: Map[Key, List[Value]] => Map[Key, List[Value]]) {
    val current = mappings.get
    val next = f(current)
    if (!mappings.compareAndSet(current, next)) {
      modify(cb, f)
    } else {
      cb()
    }
  }

  protected def get(key: Key) = mappings.get.get(key)

  protected def headOption(key: Key) = get(key).flatMap(_.headOption)

  protected def registry: Map[Key, List[Value]] = mappings.get()
}
