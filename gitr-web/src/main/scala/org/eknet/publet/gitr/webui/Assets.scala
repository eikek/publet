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

package org.eknet.publet.gitr.webui

import org.eknet.publet.web.asset.{AssetCollection, Group}
import org.eknet.publet.web.template.DefaultLayout

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.09.12 18:58
 */
object Assets extends AssetCollection {

  override def classPathBase = "/org/eknet/publet/gitr/webui/includes"

  val gitrListing = Group("gitr-web-listing")
    .add(resource("gitr-listing.js"))
    .require(DefaultLayout.Assets.jquery.name)
    .require(DefaultLayout.Assets.bootstrap.name)

  val gitrBrowser = Group("gitr-repo-browser")
    .add(resource("gitr-browser.js").noCompress) //todo: syntax errors detected while compressing
    .add(resource("gitr.css"))
    .require(DefaultLayout.Assets.jquery.name)
    .require(DefaultLayout.Assets.bootstrap.name)

  val gitrweb = Group("publet.gitrweb")
    .use(gitrListing.name, gitrBrowser.name)

}
