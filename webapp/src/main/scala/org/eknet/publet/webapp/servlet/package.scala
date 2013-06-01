package org.eknet.publet.webapp

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 26.05.13 14:35
 */
package object servlet {

  final class IteratorEnum[E](val iterator: Iterator[E]) extends java.util.Enumeration[E] {
    def hasMoreElements = iterator.hasNext
    def nextElement() = iterator.next()
  }


  final class EnumIterator[A](val enum: java.util.Enumeration[A]) extends Iterator[A] {
    def hasNext = enum.hasMoreElements
    def next() = enum.nextElement
  }

  implicit def wrapEnumeration[A](en: java.util.Enumeration[A]) = new EnumIterator(en)
  implicit def wrapIterator[A](it: Iterator[A]) = new IteratorEnum(it)
}
