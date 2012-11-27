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
import org.eknet.publet.vfs.{ContentResource, Resource}
import com.google.inject.name.Named
import org.eknet.publet.Publet
import org.eknet.publet.web.guice.{PubletModule, PubletStartedEvent}
import org.eknet.publet.vfs.Path._
import collection.JavaConversions._
import org.eknet.publet.vfs.util.MapContainer
import com.google.common.eventbus.Subscribe

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.11.12 20:07
 */
@Singleton
class ExtensionDoc @Inject() (@Named("ExtDoc") docs: java.util.Map[Class[Object], List[ContentResource]], publet: Publet) {

  private val basePath = "/publet/doc/modules/".p

  @Subscribe
  def automountDoc(event: PubletStartedEvent) {
    for (e <- docs) {
      val key = ModuleKey(e._1)
      val mapc = new MapContainer
      for (r <- e._2) mapc.addResource(r)
      publet.mountManager.mount(basePath / key.toKey, mapc)
    }
  }

  def moduleKeys: List[ModuleKey] = {
    docs.keySet().map(m => ModuleKey(m)).toList
  }

  def pathOf(key: ModuleKey): String = {
    (basePath / key.toKey.toLowerCase / docs.get(key.module).head).asString
  }
}

case class ModuleKey(module: Class[_]) {
  def toKey = module.getSimpleName.toLowerCase
  override def toString = module.getSimpleName
}
