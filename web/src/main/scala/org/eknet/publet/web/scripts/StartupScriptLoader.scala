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

package org.eknet.publet.web.scripts

import com.google.inject.{Injector, Inject, Singleton}
import org.eknet.publet.engine.scala.PubletCompiler
import org.eknet.publet.web.guice.PubletStartedEvent
import com.google.common.eventbus.{EventBus, Subscribe}
import org.eknet.publet.Publet
import org.eknet.publet.web.Config
import org.eknet.publet.vfs.{ContentType, ContentResource, ContainerResource, Path}
import grizzled.slf4j.Logging

/**
 * Goes through the server directory `.allIncludes/startup/` and compiles
 * and loads all scala source files. They are expected to contain one single
 * class that is equally named as the resource. This class is loaded and
 * instantiated using the guice injector. That means, it is also registered
 * at the global event bus.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.11.12 21:47
 */
@Singleton
class StartupScriptLoader @Inject() (compiler: PubletCompiler, config: Config, publet: Publet, injector: Injector) extends Logging {

  private[this] val scriptDir = Path(config.mainMount).toAbsolute / Publet.allIncludesPath / "startup"

  @Subscribe
  def loadScripts(event: PubletStartedEvent) {
    publet.rootContainer.lookup(scriptDir)
      .collect({ case r: ContainerResource => r})
      .map(_.children
              .filter(r => r.name.targetType == ContentType.scal)
              .collect({case r: ContentResource => r })
              .map(loadStartupScript))
  }

  private[this] def loadStartupScript(resource: ContentResource) {
    info("Loading startup script: "+ resource.name.fullName)
    val clazz = compiler.loadClass(scriptDir / resource, resource)
    injector.getInstance(clazz)
  }
}
