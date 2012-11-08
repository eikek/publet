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
import org.eknet.publet.web.scripts.WebScriptResource
import org.eknet.publet.vfs.util.{MapContainer, ClasspathContainer}
import grizzled.slf4j.Logging
import org.eknet.publet.web.NotFoundHandler
import org.eknet.publet.webeditor.EditorPaths._
import org.eknet.publet.vfs.ResourceName._
import org.eknet.publet.web.asset.AssetManager
import com.google.inject.{Inject, Singleton, Scopes, AbstractModule}
import org.eknet.publet.web.guice.{PubletModule, PubletStartedEvent, PubletBinding}
import org.eknet.publet.Publet
import com.google.common.eventbus.Subscribe
import org.eknet.publet.engine.scalate.ScalateEngine

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 16:16
 */
@Singleton
class EditorWebExtension @Inject() (publet: Publet, assetMgr: AssetManager, scalateEngine: ScalateEngine) extends Logging {

  @Subscribe
  def onStartup(ev: PubletStartedEvent) {
    val cp = new ClasspathContainer(base = "/org/eknet/publet/webeditor/includes")
    publet.mountManager.mount(editorPath, cp)

    val muc = new MapContainer()
    muc.addResource(new WebScriptResource("toc.json".rn, ListContents))
    muc.addResource(new WebScriptResource("push.json".rn, PushContents))
    muc.addResource(new WebScriptResource("edit.html".rn, new Edit))
    muc.addResource(new WebScriptResource("upload.json".rn, FileUploadHandler))
    publet.mountManager.mount(scriptPath, muc)

    val editEngine = new WebEditor('edit, scalateEngine, publet)
    publet.engineManager.addEngine(editEngine)

    assetMgr setup (
      Assets.editpageBrowser,
      Assets.blueimpFileUpload,
      Assets.blueimpCanvasToBlob,
      Assets.blueimpLoadImage,
      Assets.blueimpTmpl,
      Assets.jqueryIframeTransport,
      Assets.jqueryUiWidget,
      Assets.publetFileBrowser,
      Assets.codemirror,
      Assets.codemirrorJquery,
      Assets.editPage,
      Assets.uploadPage)

  }

}

class WebeditorModule extends AbstractModule with PubletModule with PubletBinding {
  override def binder() = super.binder()
  def configure() {
    bind[NotFoundHandler].to[CreateNewHandler] in Scopes.SINGLETON
    bind[EditorWebExtension].asEagerSingleton()
  }
}