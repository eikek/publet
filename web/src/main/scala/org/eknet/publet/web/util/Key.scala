package org.eknet.publet.web.util

case class Key[T](name: String, init: PartialFunction[Scope, T])

object Key {
  def apply[T](name: String): Key[T] = Key(name, new PartialFunction[Scope, T] {
    def apply(v1: Scope) = null.asInstanceOf[T]

    def isDefinedAt(x: Scope) = false
  })
}
