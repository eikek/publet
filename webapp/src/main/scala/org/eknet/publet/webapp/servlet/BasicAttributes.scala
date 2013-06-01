package org.eknet.publet.webapp.servlet

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.05.13 14:51
 */
trait BasicAttributes {

  private val attributes = new AtomicReference(Map.empty[String, Any])

  def getAttribute(name: String) = attributes.get.get(name).orNull.asInstanceOf[AnyRef]

  def getAttributeNames: java.util.Enumeration[String] = attributes.get.keys.iterator

  @tailrec
  final def setAttribute(name: String, `object`: Any) {
    val map = attributes.get
    val next = map.updated(name, `object`)
    if (!attributes.compareAndSet(map, next)) {
      setAttribute(name, `object`)
    }
  }

  @tailrec
  final def removeAttribute(name: String) {
    val map = attributes.get
    val next = map - name
    if (!attributes.compareAndSet(map, next)) {
      removeAttribute(name)
    }
  }

  protected def removeAll {
    attributes.set(Map.empty)
  }
}
