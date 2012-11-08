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

import com.google.inject.{Scopes, Singleton, Provides, AbstractModule}
import org.eknet.guice.squire.SquireBinder
import org.eknet.publet.web.Config
import org.eknet.gitr.{GitrMan, GitrManImpl}
import org.eknet.publet.gitr.partition.{GitPartManImpl, GitPartMan}
import com.google.inject.name.Named
import org.eknet.publet.vfs.{Path, Container}
import org.eknet.publet.web.guice.{PubletModule, PubletBinding}
import org.eknet.publet.auth.store.PermissionStore
import org.eknet.publet.gitr.auth._
import org.apache.shiro.authz.permission.PermissionResolver

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.11.12 19:36
 */
class GitrModule extends AbstractModule with PubletModule with SquireBinder with PubletBinding {

  private val contentRootRepo = Path("contentroot")

  override def binder() = super.binder()

  def configure() {
    bind[GitPartMan].to[GitPartManImpl].as[Singleton]()
    bindRequestHandler.add[GitHandlerFactory]

    bind[DefaultRepositoryStore].in(Scopes.SINGLETON)
    setOf[RepositoryStore].add[XmlRepositoryStore].in(Scopes.SINGLETON)
    setOf[PermissionStore].add[GitPermissionStore].in(Scopes.SINGLETON)
    setOf[PermissionResolver].add[GitPermissionResolver].in(Scopes.SINGLETON)
  }

  @Provides@Singleton
  def createGitrManager(config: Config): GitrMan = new GitrManImpl(config.repositories)

  @Provides@Singleton@Named("contentroot")
  def createMainPartition(gitr: GitPartMan): Container =
    gitr.getOrCreate(contentRootRepo, org.eknet.publet.gitr.partition.Config(None))

}