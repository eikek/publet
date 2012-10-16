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

package org.eknet.publet.web

import util.PropertiesMap
import org.eknet.publet.vfs.{ContentResource, Path, Container}
import org.eknet.publet.Publet
import com.google.common.eventbus.{Subscribe, EventBus}
import com.google.inject.{Singleton, Inject}
import com.google.inject.name.Named
import org.eknet.publet.event.Event
import org.eknet.publet.vfs.events.ContentWrittenEvent
import grizzled.slf4j.Logging
import org.eknet.publet.web.filter.PostReceiveEvent

/**
 * Represents the `settings.properties` file.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.10.12 21:27
 */
@Singleton
class Settings @Inject() (@Named("contentroot") contentRoot: Container, eventBus: EventBus) extends PropertiesMap(eventBus) with Logging {

  private var lastModification: Option[Long] = None

  //initial load
  reload()

  @Subscribe
  def reloadOnChange(event: ContentWrittenEvent) {
    if (event.resource.name.fullName == "settings.properties") {
      info("Reload settings due to file change")
      reload()
    }
  }

  @Subscribe
  def reloadOnPush(event: PostReceiveEvent) {
    getSettingsResource map { newFile =>
      if (lastModification.getOrElse(0L) != newFile.lastModification.getOrElse(0L)) {
        info("Reload settings due to file change")
        reload()
      }
    }
  }

  private def getSettingsResource = contentRoot.lookup(Path(Publet.allIncludes+"config/settings.properties"))

  override def file = getSettingsResource
    .collect({case cc: ContentResource => lastModification = cc.lastModification; cc})
    .map(_.inputStream)

  protected def createEvent() = SettingsReloadedEvent(this)
}

object Settings {
  def get = PubletWeb.publetSettings
  def apply(key: String) = get(key)
}

case class SettingsReloadedEvent(settings: Settings) extends Event
