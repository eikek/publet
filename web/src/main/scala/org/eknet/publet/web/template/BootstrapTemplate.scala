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

package org.eknet.publet.web.template

import org.eknet.publet.vfs.Path
import org.eknet.publet.web.{EmptyExtension, PubletWeb}
import org.eknet.publet.vfs.util.ClasspathContainer
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 18:25
 */
class BootstrapTemplate extends EmptyExtension with Logging {

  override def onStartup() {
    val publ = PubletWeb.publet
    Templates.mountJQuery(publ)
    Templates.mountHighlightJs(publ)
    publ.mountManager.mount(Path("/publet/bootstrap/"),
      new ClasspathContainer(base = "/org/eknet/publet/web/includes/bootstrap"))

    PubletWeb.scalateEngine.setDefaultLayoutUri("/publet/bootstrap/bootstrap.single.jade")
  }

}
