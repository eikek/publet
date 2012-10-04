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

import collection.JavaConversions._
import java.util.Properties
import java.io.InputStream

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.04.12 20:05
 */
abstract class PropertiesMap extends StringMap {

  protected def file: Option[InputStream]

  private val props = new Properties()

  private var reloadListeners: List[PropertiesMap => Unit] = Nil

  def listener(l:PropertiesMap=>Unit) {
    reloadListeners ::= l
  }

  def remove(l:PropertiesMap=>Unit) {
    reloadListeners = reloadListeners.filterNot(_ == l)
  }

  /**
   * Puts the key-value pair in the map. This value is only
   * kept in memory and not written to the underlying file!
   * @param key
   * @param value
   */
  def put(key: String, value: String) {
    props.setProperty(key, value)
  }

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
    reloadListeners.foreach { l => l(this) }
  }
}

trait StringMap {

  def apply(key: String): Option[String]

}