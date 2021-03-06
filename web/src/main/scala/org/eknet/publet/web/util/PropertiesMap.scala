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
import com.google.common.eventbus.EventBus
import java.util.concurrent.locks.ReentrantReadWriteLock
import org.eknet.publet.event.Event

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.04.12 20:05
 */
abstract class PropertiesMap(eventBus: EventBus) extends StringMap {

  protected def file: Option[InputStream]
  private val lock = new ReentrantReadWriteLock()
  private val props = new Properties()

  /**
   * Puts the key-value pair in the map. This value is only
   * kept in memory and not written to the underlying file!
   * @param key
   * @param value
   */
  def put(key: String, value: String) {
    lock.writeLock().lock()
    try {
      props.setProperty(key, value)
    } finally {
      lock.writeLock().unlock()
    }
  }

  def apply(key: String) = {
    lock.readLock().lock()
    try {
      Option(props.getProperty(key))
    } finally {
      lock.readLock().unlock()
    }
  }

  def keySet = {
    lock.readLock().lock()
    try {
      props.stringPropertyNames().toSet
    } finally {
      lock.readLock().unlock()
    }
  }

  /**
   * Reloads all properties from the underlying file.
   *
   */
  def reload() {
    lock.writeLock().lock()
    try {
      props.clear()
      if (file.isDefined) props.load(file.get)
    } finally {
      lock.writeLock().unlock()
    }
    eventBus.post(createEvent())
  }

  protected def createEvent(): Event
}

trait StringMap {

  def apply(key: String): Option[String]

  def reload()

  def keySet: Set[String]

  def put(key: String, value: String)

}