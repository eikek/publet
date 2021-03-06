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

package org.eknet.publet.auth.xml

import org.eknet.publet.vfs.{Path, ChangeInfo, Writeable, ContentResource}
import grizzled.slf4j.Logging
import scala.xml.{Elem, PrettyPrinter, XML}
import java.io.ByteArrayInputStream
import org.eknet.publet.auth.store.{User, UserProperty}
import org.eknet.publet.Publet
import com.google.common.eventbus.Subscribe
import org.eknet.publet.vfs.events.ContentWrittenEvent
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 01.11.12 22:36
 */
abstract class XmlResource(val source: ContentResource) extends Logging {

  private val lock = new ReentrantReadWriteLock()
  private val prettyPrinter = new PrettyPrinter(90, 2)
  private var lastModification: Option[Long] = None

  def reload() {
    lastModification = None
    reloadIfChanged()
  }

  def reloadIfChanged() = {
    if (lastModification.getOrElse(0L) != source.lastModification.getOrElse(0L)) {
      val rootElem = XML.load(source.inputStream)
      onLoad(rootElem)
      true
    } else {
      false
    }
  }

  def onLoad(rootElem: Elem)

  def write(currentUser: Option[User], message: String) {
    source match {
      case ws: Writeable => withWriteLock {
        val bin = new ByteArrayInputStream(prettyPrinter.format(toXml).getBytes("UTF-8"))
        val user = currentUser.map { u =>
          new ChangeInfo(u.get(UserProperty.fullName), u.get(UserProperty.email), message) }
        ws.writeFrom(bin, user)
        lastModification = source.lastModification
      }
      case _ =>
    }
  }

  def toXml: Elem


  def withReadLock[A](f: => A): A = {
    lock.readLock().lock()
    try {
      f
    } finally {
      lock.readLock().unlock()
    }
  }

  def withWriteLock[A](f: => A): A = {
    lock.writeLock().lock()
    try {
      f
    } finally {
      lock.writeLock().unlock()
    }
  }

}
