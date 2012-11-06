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

package org.eknet.publet.gitr

import com.google.inject.{Singleton, Provides, AbstractModule}
import org.eknet.guice.squire.SquireModule
import org.eknet.publet.web.Config
import org.eknet.gitr.{GitrMan, GitrManImpl}
import org.eknet.publet.gitr.partition.{GitPartManImpl, GitPartMan}
import com.google.inject.name.Named
import org.eknet.publet.vfs.{Path, Container}
import org.eknet.publet.gitr.web.GitHandlerFactory
import org.eknet.publet.web.guice.PubletBinding

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.11.12 19:36
 */
class GitrModule extends AbstractModule with SquireModule with PubletBinding {

  private val contentRootRepo = Path("contentroot")

  def configure() {
    bind[GitPartMan].to[GitPartManImpl].as[Singleton]()
    bindRequestHandler.add[GitHandlerFactory]
  }

  @Provides@Singleton
  def createGitrManager(config: Config): GitrMan = new GitrManImpl(config.repositories)

  @Provides@Singleton@Named("contentroot")
  def createMainPartition(gitr: GitPartMan): Container =
    gitr.getOrCreate(contentRootRepo, org.eknet.publet.gitr.partition.Config(None))


}
