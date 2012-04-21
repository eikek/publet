package org.eknet.publet.web

import javax.servlet.http.{HttpServletRequest, HttpSession}
import javax.servlet.ServletContext
import AttributeMap._


case class Key[T](name: String, init: PartialFunction[Scope, T])
object Key {
  def apply[T](name: String): Key[T] = Key(name, new PartialFunction[Scope, T] {
    def apply(v1: Scope) = null.asInstanceOf[T]
    def isDefinedAt(x: Scope) = false
  })
}

abstract sealed class Scope(name: Symbol)
object Request extends Scope('request)
object Session extends Scope('session)
object Context extends Scope('context)

trait AttributeMap {

  def scope: Scope

  def put[T: Manifest](key: Key[T], value: T) = {
    val v = get(key)
    setAttr(key.name, value)
    v
  }

  def apply[T: Manifest](key: Key[T]) = get(key)

  def get[T : Manifest](key: Key[T]) = {
    Option(getAttr(key.name)) match {
      case None => {
        if (key.init.isDefinedAt(scope)) {
          val value = key.init(scope)
          setAttr(key.name, value)
          Some(value)
        } else {
          None
        }
      }
      case Some(t) if (manifest[T].erasure.isAssignableFrom(t.getClass)) => Some(t.asInstanceOf[T])
      case _ => throw new RuntimeException("Wrong type for attribute: "+key)
    }
  }

  def remove[T: Manifest](key: Key[T]): Option[T] = {
    val value = get(key)
    removeAttr(key.name)
    value
  }

  def removeAttr(name: String)
  def setAttr(name: String, value: Any)
  def getAttr(name: String): AnyRef
  def keys: Iterable[String]
}

object AttributeMap {

  implicit def enumToIterator(en: java.util.Enumeration[_]): Iterator[String] = new Iterator[String] {
    def hasNext = en.hasMoreElements
    def next() = en.nextElement().asInstanceOf[String]
  }
}
protected class SessionMap(session: HttpSession) extends AttributeMap {
  val scope = Session

  def setAttr(name: String, value: Any) {
    session.setAttribute(name, value)
  }

  def getAttr(name: String) = session.getAttribute(name)

  def keys = new Iterable[String] {
    def iterator = session.getAttributeNames
  }

  def removeAttr(name: String) {
    session.removeAttribute(name)
  }
}

protected class RequestMap(req: HttpServletRequest) extends AttributeMap {
  val scope = Request

  def setAttr(name: String, value: Any) {
    req.setAttribute(name, value)
  }

  def getAttr(name: String) = req.getAttribute(name)

  def keys = new Iterable[String] {
    def iterator = req.getAttributeNames
  }

  def removeAttr(name: String) {
    req.removeAttribute(name)
  }
}

protected class ContextMap(ctx: ServletContext) extends AttributeMap {
  val scope = Context

  def setAttr(name: String, value: Any) {
    ctx.setAttribute(name, value)
  }

  def getAttr(name: String) = ctx.getAttribute(name)

  def keys = new Iterable[String] {
    def iterator = ctx.getAttributeNames
  }

  def removeAttr(name: String) {
    ctx.removeAttribute(name)
  }
}
