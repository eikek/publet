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

package org.eknet.publet.ext

import org.eknet.publet.Publet
import org.eknet.publet.web.scripts.WebScriptResource
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.{PubletWeb, WebExtension}
import grizzled.slf4j.Logging
import org.eknet.publet.vfs.util.{ClasspathContainer, MapContainer}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 20:17
 */
class ExtWebExtension extends WebExtension with Logging {

  def onStartup() {
    info("Installing publet extensions...")
    ExtWebExtension.install(PubletWeb.publet)
  }

  def onShutdown() {
  }
}

object ExtWebExtension {

  val extScriptPath = Path("/publet/ext/scripts/")

  def install(publet: Publet) {
    import org.eknet.publet.vfs.ResourceName._
    val muc = new MapContainer()
    muc.addResource(new WebScriptResource("captcha.png".rn, CaptchaScript))
    muc.addResource(new WebScriptResource("contact.html".rn, MailContact))
    publet.mountManager.mount(extScriptPath, muc)

    val cont = new ClasspathContainer(base = "/org/eknet/publet/ext/includes")
    publet.mountManager.mount(Path("/publet/ext/includes/"), cont)
  }
}
