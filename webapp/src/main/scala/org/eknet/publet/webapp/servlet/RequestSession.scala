package org.eknet.publet.webapp.servlet

import javax.servlet.http.HttpSession
import java.util.UUID
import javax.servlet.ServletContext

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.05.13 12:37
 */
class RequestSession(context: ServletContext) extends HttpSession with BasicAttributes {
  private val created = System.currentTimeMillis()
  private val id = UUID.randomUUID().toString

  def getCreationTime = created

  def getId = id

  def getLastAccessedTime = created

  def getServletContext = context

  def setMaxInactiveInterval(interval: Int) {}

  def getMaxInactiveInterval = -1

  //deprecated
  def getSessionContext = throw new UnsupportedOperationException("deprecated")

  //deprecated
  def getValue(name: String) = throw new UnsupportedOperationException("deprecated")

  //deprecated
  def getValueNames = throw new UnsupportedOperationException("deprecated")

  //deprecated
  def putValue(name: String, value: Any) {
    throw new UnsupportedOperationException("deprecated")
  }

  //deprecated
  def removeValue(name: String) {
    throw new UnsupportedOperationException("deprecated")
  }

  def invalidate() {
    removeAll
  }

  def isNew = true
}
