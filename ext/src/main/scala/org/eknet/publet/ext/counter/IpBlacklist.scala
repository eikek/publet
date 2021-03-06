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

package org.eknet.publet.ext.counter

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import org.eknet.publet.web.util.{StringMap, PropertiesMap}
import org.eknet.publet.web.SettingsReloadedEvent
import com.google.common.eventbus.Subscribe

/**
 * Uses `ext.counter.blacklist.*` keys in the given [[org.eknet.publet.web.util.PropertiesMap]]
 * to implement a ip blacklist. Hostnames are re-resolved after a certain amount of time has
 * elapsed.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.10.12 10:46
 */
class IpBlacklist(m: StringMap) extends IpPropertyUtil {
  private val defaultUpdateSpan = (15, TimeUnit.HOURS)
  private val lock = new ReentrantReadWriteLock()
  private val nextUpdate = new AtomicLong(getUpdateSpan + System.currentTimeMillis())
  private var ipcache = loadIps(m)

  private def getUpdateSpan = resolveInterval._2.toMillis(resolveInterval._1)

  private def resolveInterval = {
    try {
      val hours = m("ext.counter.blacklistResolveInterval").getOrElse("15").toInt
      if (hours <= 0) {
        defaultUpdateSpan
      } else {
        (hours, TimeUnit.HOURS)
      }
    } catch {
      case e: NumberFormatException => defaultUpdateSpan
    }
  }

  private def loadIps(m: StringMap) = {
    nextUpdate.set(System.currentTimeMillis() + getUpdateSpan)
    Map() ++ m.blacklistHostnames
      .map(name => name.resolveIp.map(_ -> name))
      .flatten
  }

  def reloadIps() {
    lock.writeLock().lock()
    try {
      ipcache = loadIps(m)
    } finally {
      lock.writeLock().unlock()
    }
  }

  /**
   * Checks whether the given ip address is listed in the settings
   * ip blacklist.
   *
   * Hostnames are re-resolved in a regular interval within this
   * method.
   *
   * @param ip
   * @return
   */
  def isListed(ip: String): Boolean = {
    //is ip address listed?
    m(keyPrefix + ip).map(_.toBoolean) getOrElse {

      //else lookup hostname, re-resolve if necessary
      lock.readLock().lock()
      if (nextUpdate.get() <= System.currentTimeMillis()) {
        lock.readLock().unlock()
        lock.writeLock().lock()
        try {
          ipcache = loadIps(m)
        } finally {
          lock.readLock().lock()
          lock.writeLock().unlock()
        }
      }
      try {
        ipcache.get(ip).flatMap(name => m(keyPrefix+name).map(_.toBoolean)).getOrElse(false)
      } finally {
        lock.readLock().unlock()
      }

    }
  }
}
