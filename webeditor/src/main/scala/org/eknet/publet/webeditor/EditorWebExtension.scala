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

package org.eknet.publet.webeditor

import actions._
import org.eknet.publet.Publet
import org.eknet.publet.web.scripts.WebScriptResource
import org.eknet.publet.vfs.util.{MapContainer, ClasspathContainer}
import grizzled.slf4j.Logging
import org.eknet.publet.engine.scalate.ScalateEngine
import org.eknet.publet.web.{PubletWeb, WebExtension}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 16:16
 */
class EditorWebExtension extends WebExtension with Logging {

  def onStartup() {
    info("Installing webeditor ...")
    EditorWebExtension.setup(PubletWeb.publet, PubletWeb.scalateEngine)
    PubletWeb.contextMap.put(PubletWeb.notFoundHandlerKey, new CreateNewHandler())
  }

  def onShutdown() {}
}

object EditorWebExtension {

  import EditorPaths._

  def setup(publet: Publet, scalateEngine: ScalateEngine) {
    import org.eknet.publet.vfs.ResourceName._

    val cp = new ClasspathContainer(base = "/org/eknet/publet/webeditor/includes")
    publet.mountManager.mount(editorPath, cp)

    val muc = new MapContainer()
    muc.addResource(new WebScriptResource("toc.json".rn, ListContents))
    muc.addResource(new WebScriptResource("push.json".rn, PushContents))
    muc.addResource(new WebScriptResource("browser.js".rn, BrowserJs))
    muc.addResource(new WebScriptResource("edit.html".rn, new Edit))
    muc.addResource(new WebScriptResource("upload.json".rn, FileUploadHandler))
    muc.addResource(new WebScriptResource("thumb.png".rn, Thumbnailer))
    publet.mountManager.mount(scriptPath, muc)

    val editEngine = new WebEditor('edit, scalateEngine)
    publet.engineManager.addEngine(editEngine)
  }

}
