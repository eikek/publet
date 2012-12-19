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

import org.eknet.publet.vfs.util.{SimpleContentResource, MapContainer, ClasspathContainer}
import org.eknet.publet.vfs.{ResourceName, Path}
import grizzled.slf4j.Logging
import org.eknet.publet.web.asset.{AssetManager, Group, AssetCollection}
import org.eknet.publet.web.template.DefaultLayout
import com.google.common.eventbus.Subscribe
import org.eknet.publet.web.guice.PubletStartedEvent
import org.eknet.publet.Publet
import com.google.inject.{Inject, Singleton}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.05.12 22:29
 */
@Singleton
class PubletDocExtension @Inject() (publet: Publet, assetMgr: AssetManager) extends Logging with AssetCollection {

  override def classPathBase = "/org/eknet/publet/doc/resources/_includes/"

  val css = Group("publet.doc")
    .forPath("/publet/doc/**")
    .add(resource("doc.css"))
    .require(DefaultLayout.Assets.bootstrap.name)

  val docResources = List(
    DocPage("intro.md", "Introduction"),
    DocPage("install.md", "Installation"),
    DocPage("usage.md", "Basic Usage"),
    MenuName("Good to know"),
    DocPage("conventions.md", "Conventions"),
    DocPage("configuration.md", "Configuration"),
    DocPage("security.jade", "Security"),
    DocPage("git.md", "Git"),
    MenuName("Other Topics"),
    DocPage("page-layouts.md", "Layouts"),
    DocPage("assets.jade", "Assets"),
    DocPage("partitions.md", "Partitions"),
    DocPage("scala-scripts.md", "Scala Scripts"),
    DocPage("redirects.md", "Redirects"),
    MenuName("Extensions"),
    DocPage("ext_intro.md", "Introduction"),
    DocPage("ext_guice.md", "Guice"),
    DocPage("ext_hooks.jade", "Hooks"),
    DocPage("ext_installed.jade", "Installed Extensions")
  )

  @Subscribe
  def mountResources(ev: PubletStartedEvent) {
    assetMgr setup css
    assetMgr setup Group("default")
      .use(css.name)

    val cont = new ClasspathContainer(base = "/org/eknet/publet/doc/resources")
    publet.mountManager.mount(Path("/publet/doc/incl"), cont)

    val pageContainer = new MapContainer
    pageContainer.addResource(new SidebarResource(docResources))
    docResources map (dr => dr match {
      case p: DocPage => {
        pageContainer.addResource(new SimpleContentResource(p.resource.name.invisibleName, p.resource))
        val docr = new DocPageResource(p)
        pageContainer.addResource(docr)
        if (p == docResources.head) {
          pageContainer.addResource(new SimpleContentResource(ResourceName("index.md").withExtension(docr.name.ext), docr))
        }
      }
      case _ =>
    })
    publet.mountManager.mount(Path("/publet/doc"), pageContainer)
  }

}
