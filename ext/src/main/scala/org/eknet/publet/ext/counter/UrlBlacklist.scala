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

import org.eknet.publet.web.util.StringMap
import com.google.common.base.Splitter
import org.eknet.publet.Glob
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.11.12 12:17
 */
class UrlBlacklist(config: StringMap) {
  import collection.JavaConversions._

  private val lock = new ReentrantReadWriteLock()
  var urlList = loadList()
  var isBlacklist = loadIsBlacklist()

  private[this] def loadList() = {
    config("ext.counter.url.list")
      .map(str => Splitter.on(",").omitEmptyStrings().trimResults().split(str).map(Glob(_)))
      .map(_.toList)
      .getOrElse(List(Glob("**")))
  }

  private[this] def loadIsBlacklist() = {
    config("ext.counter.url.list.blacklist").map(_.toBoolean).getOrElse(false)
  }

  def reloadUrlList() {
    lock.writeLock().lock()
    try {
      this.urlList = loadList()
      this.isBlacklist = loadIsBlacklist()
    } finally {
      lock.writeLock().unlock()
    }
  }

  def isListed(uri: String): Boolean = {
    lock.readLock().lock()
    try {
      val inList = urlList.foldLeft(false)((b, glob) => if (b) true else glob.matches(uri))
      //not (inList XOR isBlacklist):
      (inList, isBlacklist) match {
        case (true, true) => true  //is in blacklist
        case (true, false) => false //is in whitelist
        case (false, true) => false //is not in blacklist
        case (false, false) => true //not in whitelist
      }
    } finally {
      lock.readLock().unlock()
    }
  }
}
