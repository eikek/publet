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

package org.eknet.publet.web

import scala.collection.JavaConversions._
import java.util.ServiceLoader
import grizzled.slf4j.Logging
import javax.servlet.http.HttpServletRequest
import com.google.inject.Binder

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.05.12 15:45
 */
object WebExtensionLoader extends Logging {

  private lazy val loadExtensions = ServiceLoader.load(classOf[WebExtension])
    .iterator()
    .withFilter(ext => Config(ext.getClass.getName).getOrElse("true").toBoolean)
    .toList

  /**
   * The class names of all installed extensions.
   *
   */
  lazy val extensionNames = loadExtensions.map(_.getClass.getName).sorted

  private def safely[A](errorMsg: => String)(body: () => A): Option[A] = {
    try {
      Some(body())
    } catch {
      case e: Exception => error(errorMsg, e); None
    }
  }

  /**
   * Looks up all [[org.eknet.publet.web.WebExtension]]s using the
   * service-loader pattern from java.
   * If they are not configured inactive, each extensions is installed.
   *
   * Extensions can be configured to not run on startup, if the config
   * file contains an entry of the complete class name and a value of `false`
   */
  private[web] def onStartup() {
    for (ext <- loadExtensions) {
      info("Installing extension: "+ ext.getClass.getName)
      safely("Error on startup for extension '"+ext.getClass+"'!")(ext.onStartup)
    }
  }

  private[web] def onShutdown() {
    loadExtensions.foreach(ext => safely("Error on shutdown for extension '"+ext+"'!")(ext.onShutdown))
  }

  private[web] def executeBeginRequest(req: HttpServletRequest): HttpServletRequest = {
    loadExtensions.foldLeft(req)((r1, ext) => {
      safely("Exception invoking onBeginRequest of extension '"+ ext +"'!") { () =>
        ext.onBeginRequest(r1)
      } getOrElse(r1)
    })
  }

  private[web] def executeEndRequest(req:HttpServletRequest) {
    loadExtensions.foreach(ext => safely("Exception invoking onEndRequest of extension '"+ ext +"'!") { () =>
      ext.onEndRequest(req)
    })
  }

  def getModules = loadExtensions.map(_.getModule).flatten

}
