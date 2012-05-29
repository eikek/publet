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

package org.eknet.publet.gitr.web

import org.eknet.publet.web.{PubletWeb, WebExtension}
import org.eknet.publet.vfs.ResourceName._
import org.eknet.publet.vfs.Path._
import org.eknet.publet.web.scripts.WebScriptResource
import scripts.{GitrView, GitrRepoList, GitrCreate}
import org.eknet.publet.vfs.util.{UrlResource, MapContainer}
import java.net.URL

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.05.12 20:34
 */
class GitrWebExtension extends WebExtension {

  def onStartup() {

    val pages = new MapContainer()
    pages.addResource(new UrlResource(toUrl("browse.page"), "browse.page".rn))
    pages.addResource(new UrlResource(toUrl("index.page"), "index.page".rn))
    pages.addResource(new UrlResource(toUrl("gitr-browser.js"), "gitr-browser.js".rn))
    pages.addResource(new UrlResource(toUrl("gitr-listing.js"), "gitr-listing.js".rn))
    pages.addResource(new WebScriptResource("gitrcreate.json".rn, new GitrCreate()))
    pages.addResource(new WebScriptResource("gitr-repolist.json".rn, new GitrRepoList()))
    pages.addResource(new WebScriptResource("gitrview.json".rn, new GitrView()))
    PubletWeb.publet.mountManager.mount("/gitr".p, pages)
  }

  private def toUrl(name: String): URL = {
    classOf[GitrWebExtension].getResource("/org/eknet/publet/gitr/web/includes/"+ name)
  }

  def onShutdown() {}
  
}
