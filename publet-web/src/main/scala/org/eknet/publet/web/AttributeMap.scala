package org.eknet.publet.web

import javax.servlet.http.{HttpServletRequest, HttpSession}
import javax.servlet.ServletContext
import AttributeMap._

case class Key[T](name: String)

trait AttributeMap {
  def put[T: Manifest](key: Key[T], value: T) = {
    val v = get(key)
    setAttr(key.name, value)
    v
  }

  def get[T : Manifest](key: Key[T]) = {
    Option(getAttr(key.name)) match {
      case None => None
      case Some(t) if (manifest[T].erasure.isAssignableFrom(t.getClass)) => Some(t.asInstanceOf[T])
      case _ => throw new RuntimeException("Wrong type for attribute: "+key)
    }
  }

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
  def setAttr(name: String, value: Any) {
    session.setAttribute(name, value)
  }

  def getAttr(name: String) = session.getAttribute(name)

  def keys = new Iterable[String] {
    def iterator = session.getAttributeNames
  }
}

protected class RequestMap(req: HttpServletRequest) extends AttributeMap {
  def setAttr(name: String, value: Any) {
    req.setAttribute(name, value)
  }

  def getAttr(name: String) = req.getAttribute(name)

  def keys = new Iterable[String] {
    def iterator = req.getAttributeNames
  }
}

protected class ContextMap(ctx: ServletContext) extends AttributeMap {
  def setAttr(name: String, value: Any) {
    ctx.setAttribute(name, value)
  }

  def getAttr(name: String) = ctx.getAttribute(name)

  def keys = new Iterable[String] {
    def iterator = ctx.getAttributeNames
  }
}

protected class ParameterMap(req: HttpServletRequest) extends AttributeMap {
  def setAttr(name: String, value: Any) {
    throw new RuntimeException("Request parameters are not writeable")
  }

  def getAttr(name: String) = req.getParameter(name)

  def keys = new Iterable[String] {
    def iterator = req.getParameterNames
  }
}