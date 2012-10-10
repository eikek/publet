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

import event.Event
import util.PropertiesMap
import org.eknet.publet.vfs.{ContentResource, Path, Container}
import org.eknet.publet.Publet
import com.google.common.eventbus.EventBus
import com.google.inject.{Singleton, Inject}
import com.google.inject.name.Named

/**
 * Represents the `settings.properties` file.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.10.12 21:27
 */
@Singleton
class Settings @Inject() (@Named("contentroot") contentRoot: Container, eventBus: EventBus) extends PropertiesMap(eventBus) {

  //initial load
  reload()

  override def file = contentRoot.lookup(Path(Publet.allIncludes+"config/settings.properties"))
    .collect({case cc: ContentResource => cc})
    .map(_.inputStream)

  protected def createEvent() = SettingsReloadedEvent(this)
}

object Settings {
  def get = PubletWeb.publetSettings
  def apply(key: String) = get(key)
}

case class SettingsReloadedEvent(settings: Settings) extends Event
