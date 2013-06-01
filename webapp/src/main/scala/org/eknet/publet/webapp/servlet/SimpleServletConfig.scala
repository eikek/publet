package org.eknet.publet.webapp.servlet

import javax.servlet.{ServletContext, ServletConfig}
import java.util

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.05.13 01:26
 */
class SimpleServletConfig(servletName: String, context: ServletContext, params: Map[String, String] = Map.empty) extends ServletConfig {
  def getServletName = servletName

  def getServletContext = context

  def getInitParameter(name: String) = params.get(name).orNull

  def getInitParameterNames = new util.Enumeration[String] {
    val entries = params.iterator
    def hasMoreElements = entries.hasNext
    def nextElement() = entries.next()._2
  }
}
