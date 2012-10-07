/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.publet.web.util

import javax.servlet.http.{HttpServletRequest, HttpSession}

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
      case Some(t) => Some(t.asInstanceOf[T])
      case x@_ => {
        sys.error("Wrong type for attribute: "+key+" and value: "+ x + " erasure: "+ manifest[T].erasure)
      }
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

  def apply(request: HttpServletRequest): AttributeMap = new RequestMap(request)
  def apply(session: HttpSession): AttributeMap = new SessionMap(session)

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
}

