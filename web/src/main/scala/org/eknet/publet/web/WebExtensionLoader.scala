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

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.05.12 15:45
 */
object WebExtensionLoader extends Logging {

  /**
   * Looks up all [[org.eknet.publet.web.WebExtension]]s using the
   * service-loader pattern from java.
   * If they are not configured inactive, each extensions is installed.
   *
   * Extensions can be configured to not run on startup, if the config
   * file contains an entry of the complete class name and a value of `false`
   */
  def installWebExtensions() {
    for (ext <- loadExtensions) {
      val name = ext.getClass.getName
      if (Config(name).getOrElse("true").toBoolean) {
        info("Installing extension: "+ name)
        ext.onStartup()
      } else {
        info("Extension '"+name+"' not installed.")
      }
    }
  }

  private lazy val loadExtensions: List[WebExtension] = {
    val loader = ServiceLoader.load(classOf[WebExtension])
    loader.iterator().toList
  }

  def executeBeginRequest() {
    for (ext <- loadExtensions) {
      try {
        ext.onBeginRequest()
      }
      catch {
        case e:Exception => error("Exception invoking onBeginRequest of extension '"+ ext +"'!", e)
      }
    }
  }

  def executeEndRequest() {
    for (ext <- loadExtensions) {
      try {
        ext.onEndRequest()
      }
      catch {
        case e:Exception => error("Exception invoking onEndRequest of extension '"+ ext +"'!", e)
      }
    }
  }
}
