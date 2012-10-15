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

package org.eknet.publet.web.guice

import com.google.inject.{Module, Inject, Singleton}
import org.eknet.publet.web.{WebExtension, Config}
import collection.JavaConversions._
import javax.servlet.http.HttpServletRequest
import grizzled.slf4j.Logging

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 15.10.12 14:09
 * 
 */
trait ExtensionManager {
  def executeBeginRequest(req: HttpServletRequest): HttpServletRequest
  def executeEndRequest(req:HttpServletRequest)
}

@Singleton
class DefaultExtensionManager @Inject()(webext: java.util.Set[WebExtension]) extends ExtensionManager with Logging {


  private def safely[A](errorMsg: => String)(body: () => A): Option[A] = {
    try {
      Some(body())
    } catch {
      case e: Exception => error(errorMsg, e); None
    }
  }

  def executeBeginRequest(req: HttpServletRequest): HttpServletRequest = {
    webext.foldLeft(req)((r1, ext) => {
      safely("Exception invoking onBeginRequest of extension '"+ ext +"'!") { () =>
        ext.onBeginRequest(r1)
      } getOrElse(r1)
    })
  }

  def executeEndRequest(req:HttpServletRequest) {
    webext.foreach(ext => safely("Exception invoking onEndRequest of extension '"+ ext +"'!") { () =>
      ext.onEndRequest(req)
    })
  }
}

trait PubletModule extends Module