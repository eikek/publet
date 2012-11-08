package org.eknet.publet.ext.counter

import java.util
import org.eknet.publet.web.util.{PubletWeb, ClientInfo}
import com.google.inject.ConfigurationException

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.06.12 19:41
 */
trait CounterService {

  /**
   * Returns the number of accesses to the given path. The path
   * is starting from the context-path.
   *
   * @param uriPath
   * @return
   */
  def getPageCount(uriPath: String): Long

  /**
   * Returns the last time the given uri has been accessed.
   *
   * @param uriPath
   * @return
   */
  def getLastAccess(uriPath: String): Long

  /**
   * Returns all uris sorted by last access time, descending.
   * @return
   */
  def getUrisByAccess: List[(String, Long)]

  /**
   * Returns all uris sorted by access count, descending.
   *
   * @return
   */
  def getUrisByCount: List[(String, Long)]

  /**
   * Returns the last time the given uri has been accessed in a standard
   * string format. The time is formatted according to the given locale
   * (or the default locale).
   *
   * @param uri
   * @param locale
   * @return
   */
  def getLastAccessString(uri: String, locale: Option[util.Locale] = None): String

  /**
   * "Collect" the page uri and client info. This increments
   * the page counter and sets a new last-access timestamp.
   *
   * @param uriPath
   * @param info
   */
  def collect(uriPath: String, info: ClientInfo)

  /**
   * Returns the md5 has of the resource at the specified uri. If no resource
   * is found, [[scala.None]] is returned.
   *
   * @param uriPath
   * @return
   */
  def getMd5(uriPath: String): Option[String]
}

object CounterService {

  /**
   * Returns the [[org.eknet.publet.ext.counter.CounterService]] from
   * the web environment.
   *
   * @return
   */
  def apply(): CounterService = PubletWeb.instance[CounterService].get

  def serviceOption = {
    try {
      Some(apply())
    } catch {
      case e: ConfigurationException => None
    }
  }
}
