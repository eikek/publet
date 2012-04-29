package org.eknet.publet.web.util

import collection.JavaConversions._
import java.util.Properties
import java.io.InputStream

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.04.12 20:05
 */
abstract class PropertiesMap extends StringMap {

  protected def file: Option[InputStream]

  private val props = new Properties();

  def apply(key: String) = Option(props.getProperty(key))

  def keySet = props.stringPropertyNames().toSet

  /**
   * Reloads all properties from the underlying file.
   *
   */
  def reload() {
    synchronized {
      props.clear()
      if (file.isDefined) props.load(file.get)
    }
  }
}

trait StringMap {

  def apply(key: String): Option[String]

}