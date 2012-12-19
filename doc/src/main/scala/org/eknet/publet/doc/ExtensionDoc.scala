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

import com.google.inject.{Inject, Singleton}
import org.eknet.publet.vfs._
import com.google.inject.name.Named
import org.eknet.publet.Publet
import org.eknet.publet.web.guice.PubletModule
import org.eknet.publet.vfs.Path._
import collection.JavaConversions._
import org.eknet.publet.vfs.util.{SimpleContentResource, MapContainer}
import com.google.common.eventbus.Subscribe
import org.eknet.publet.web.guice.PubletStartedEvent
import org.eknet.publet.engine.scalate.ScalateEngine

/**
 * Mounts all documentation resources contributed by extension modules and mounts them below
 * the `publet/doc` content tree. It exposes a single page to conveniently access the documentation
 * of all included extensions from one place.
 *
 * There is the following convention that must apply for all contributions:
 *
 * - The contributed list of resources must not be empty.
 * - The first resource in the list is considered to be the main entry point in the documentation
 * - the first resource must not be named "index.*" and must be some valid template (not an image
 *   or any other data)
 *
 * The list of contributed resources are all mounted in one folder using an "invisible name". The
 * resources are wrapped with a template that renders a headline and a simple sidebar. The idea is
 * to have a simple documentation view that allows to create permalinks to each site.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.11.12 20:07
 */
@Singleton
class ExtensionDoc @Inject() (@Named("ExtDoc") docs: java.util.Map[PubletModule, List[ContentResource]], publet: Publet, scalateEngine: ScalateEngine) {

  private val basePath = "/publet/doc/extensions".p

  @Subscribe
  def automountDoc(event: PubletStartedEvent) {
    for (e <- docs if (!e._2.isEmpty)) {
      val key = ModuleKey(e._1)
      val mapc = new MapContainer
      for (r <- e._2) {
        if (scalateEngine.engine.extensions.contains(r.name.ext)) {
          mapc.addResource(new SimpleContentResource(r.name.invisibleName, r))
          mapc.addResource(new SimpleContentResource(
            r.name.withExtension("page"), wrapContent(key, pathOf(key) / r.name.invisibleName) ))
        } else {
          mapc.addResource(r)
        }
      }
      mapc.addResource(new SimpleContentResource(ResourceName("index.page"), wrapContent(key, getContentSitePath(key))))
      publet.mountManager.mount(pathOf(key), mapc)
    }
  }

  /**
   * Returns a list of all contributed documentations.
   *
   */
  lazy val moduleKeys: List[ModuleKey] = {
    docs.keySet().map(m => ModuleKey(m)).toList.sorted
  }

  /**
   * Returns the base path for the given module that points to a container that holds
   * all contributed resources from the given module.
   *
   * @param key
   * @return
   */
  def pathOf(key: ModuleKey): Path = basePath / key.toKey.toLowerCase

  /**
   * Returns an url-encoded string denoting the path to a resource with
   * the given name from the given module.
   *
   * @param key
   * @param name
   * @return
   */
  def asUrlString(key: ModuleKey, name: ResourceName): String = (pathOf(key) / name).asUrlString

  /**
   * Returns an url-encoded string that is the path to the generated documentation
   * site for the given module.
   *
   * @param key
   * @return
   */
  def getIndexHtml(key:ModuleKey) = asUrlString(key, ResourceName("index.html"))

  def getContentSitePath(key: ModuleKey) = pathOf(key) / docs.get(key.module).head.name.invisibleName

  /**
   * Creates a page template that renders a sidebar with a list of extensions
   * and a default header. The given path is included below the header.
   *
   * @param key
   * @param sourcePath
   * @return
   */
  private[this] def wrapContent(key: ModuleKey, sourcePath: Path): Content = {
    val menu = moduleKeys.map(currentKey => {
      """      li(${active})
        |        a.pill(href="${moduleLink}")
        |          i.icon-chevron-right
        |          | ${moduleName}
      """.stripMargin
        .replace("${moduleLink}", getIndexHtml(currentKey))
        .replace("${moduleName}", currentKey.displayName)
        .replace("${active}", if (key == currentKey) "class=\"active\"" else "")
    }).mkString("    ul.nav.nav-pills.nav-stacked \n", "\n", "")
    val content =
      """---
        |title: ${moduleName} - Publet Documentation
        |
        |--- name:navigationBar pipeline:jade
        |=include("../../_includes/nav.jade")
        |
        |--- name:content pipeline:jade
        |=include("../../_includes/header.jade")
        |
        |.row
        |  .span2
        |    ul.nav.nav-pills
        |      li
        |        a.pill(href="../../")
        |          i.icon-chevron-left
        |          | Overview
        |    h4 Installed Extensions
        |${menu}
        |  .span9
        |    p.alert.alert-info
        |      | Module Class: ${moduleClass}
        |    =include("${modulePath}")
        |
        |""".stripMargin.replace("${moduleName}", key.displayName)
                        .replace("${modulePath}", sourcePath.asString)
                        .replace("${moduleClass}", key.module.getClass.getName)
                        .replace("${menu}", menu)


    Content(content, ContentType.page)
  }
}

case class ModuleKey(module: PubletModule) extends Ordered[ModuleKey] {
  def toKey = module.getClass.getSimpleName.toLowerCase
  def displayName = module.toString match {
    case n if (!n.contains("@")) => n
    case _ => module.getClass.getSimpleName
  }
  override def toString = module.toString

  def compare(that: ModuleKey) = displayName.compare(that.displayName)
}