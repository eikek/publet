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

import org.eknet.publet.web.util.PropertiesMap
import org.eknet.publet.web.PubletWeb
import java.net.{UnknownHostException, InetAddress}
import grizzled.slf4j.Logging
import com.google.common.cache.{RemovalNotification, RemovalListener}
import java.util.concurrent.Callable

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.10.12 00:54
 */
private[counter] trait IpPropertyUtil extends Logging {

  val keyPrefix = "ext.counter.blacklist."

  implicit def strToResolver(n:String) = new IpResolver(n)
  implicit def mapToHelper(m: PropertiesMap) = new HostnameMap(m)

  class IpResolver(name: String) {
    def resolveIp = {
      try {
        val ip = InetAddress.getByName(name).getHostAddress
        info("Resolved hostname '" + name + "'. Add '" + ip + "' to counter blacklist...")
        Some(ip)
      }
      catch {
        case e:UnknownHostException => {
          error("Cannot resolve hostname '"+name+"'! Cannot add to counter blacklist.")
          None
        }
      }
    }
  }

  class HostnameMap(m: PropertiesMap) {
    def blacklistHostnames = m.keySet.filter(_.startsWith(keyPrefix))
      .map(key => key.substring(keyPrefix.length))
      .collect({ case hn if (hn.matches("^[a-zA-Z]+")) => hn})
  }

  implicit def toListener[K,V](f: RemovalNotification[K,V]=>Unit) = new RemovalListenerFun[K, V](f)
  class RemovalListenerFun[K,V](f: RemovalNotification[K,V] => Unit) extends RemovalListener[K,V] {
    def onRemoval(notification: RemovalNotification[K, V]) {
      f(notification)
    }
  }
}
