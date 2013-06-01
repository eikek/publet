package org.eknet.publet.webapp.servlet

import javax.servlet.ServletContext
import com.typesafe.scalalogging.slf4j.Logging
import java.util
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import akka.actor.ActorSystem
import org.eknet.publet.actor.Publet
import org.eknet.publet.content.{FsContent, Content, Folder}
import org.eknet.publet.content.Source.UrlSource
import org.eknet.publet.content.Resource.SimpleContent

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.05.13 00:25
 */
class MiniServletContext(system: ActorSystem, initParams: Map[String, String] = Map.empty) extends BasicAttributes with ServletContext with Logging {

  private val publet = Publet(system)
  import collection.JavaConverters._

  def getContextPath = ""

  def getContext(uripath: String) = null

  def getMajorVersion = 2
  def getMinorVersion = 5

  def getMimeType(file: String) = publet.documentRoot().find(file) match {
    case Some(f: Folder) => null
    case Some(c: Content) => c.contentType.toString
    case Some(r) => r.name.contentType.map(_.toString).orNull
    case _ => null
  }

  def getResourcePaths(path: String) = {
    publet.documentRoot().find(path) match {
      case Some(f: Folder) => f.children.map(_.name.fullName).toSet.asJava
      case _ => Set.empty.asJava
    }
  }

  def getResource(path: String) = publet.documentRoot().find(path) match {
    case Some(SimpleContent(name, UrlSource(url))) => url
    case _ => null
  }

  def getResourceAsStream(path: String) = publet.documentRoot().find(path) match {
    case Some(c: Content) => c.inputStream
    case _ => null
  }

  def getRequestDispatcher(path: String) = ???

  def getNamedDispatcher(name: String) = ???

  //deprecated
  def getServlet(name: String) = throw new UnsupportedOperationException

  //deprecated
  def getServlets = throw new UnsupportedOperationException

  //deprecated
  def getServletNames = throw new UnsupportedOperationException

  def log(msg: String) {
    logger.info(msg)
  }

  def log(exception: Exception, msg: String) {
    logger.error(msg, exception)
  }

  def log(message: String, throwable: Throwable) {
    logger.error(message, throwable)
  }

  def getRealPath(path: String) = publet.documentRoot().find(path) match {
    case Some(c: FsContent) => c.file.toAbsolutePath.toString
    case _ => null
  }

  def getServerInfo = "spray-can wrapper"

  def getInitParameter(name: String) = initParams.get(name).orNull

  def getInitParameterNames = initParams.keys.iterator


  def getServletContextName = null
}
