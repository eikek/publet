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

package org.eknet.publet.doc

import org.eknet.publet.vfs.util.ClasspathContainer
import org.eknet.publet.web.{EmptyExtension, PubletWeb}
import org.eknet.publet.vfs.Path
import grizzled.slf4j.Logging
import org.eknet.publet.web.asset.{AssetManager, Group, AssetCollection}
import org.eknet.publet.web.template.DefaultLayout

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.05.12 22:29
 */
class PubletDocExtension extends EmptyExtension with Logging with AssetCollection {

  override def classPathBase = "/org/eknet/publet/doc/resources/_includes/"

  val css = Group("publet.doc")
    .forPath("/publet/doc/**")
    .add(resource("doc.css"))
    .require(DefaultLayout.Assets.bootstrap.name)

  override def onStartup() {
    AssetManager.service setup css
    AssetManager.service setup Group("default")
      .use(css.name)

    val cont = new ClasspathContainer(base = "/org/eknet/publet/doc/resources")
    PubletWeb.publet.mountManager.mount(Path("/publet/doc"), cont)
  }

}
