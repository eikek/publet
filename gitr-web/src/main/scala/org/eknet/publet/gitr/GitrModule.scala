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

import com.google.inject._
import org.eknet.guice.squire.{SquireModule, SquireBinder}
import org.eknet.publet.web.{PartitionMount, Config}
import org.eknet.gitr.{GitrMan, GitrManImpl}
import org.eknet.publet.gitr.partition.{GitPartManImpl, GitPartMan}
import com.google.inject.name.Named
import org.eknet.publet.vfs.{Path, Container}
import org.eknet.publet.web.guice.{AbstractPubletModule, PubletModule, PubletBinding}
import org.eknet.publet.auth.store.{ResourceSetStore, ResourceSetStoreAdapter, PermissionStore}
import org.eknet.publet.gitr.auth._
import org.apache.shiro.authz.permission.PermissionResolver
import org.eknet.publet.gitr.webui.scripts.GitrControl
import org.apache.shiro.realm.Realm
import org.eknet.publet.web.util.AppSignature

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.11.12 19:36
 */
class GitrModule extends AbstractPubletModule with PubletModule with PubletBinding {

  private val contentRootRepo = Path("contentroot")

  def configure() {
    bind[GitPartMan].to[GitPartManImpl].as[Singleton]()
    bindRequestHandler.add[GitHandlerFactory]
    bind[RepositoryService].in(Scopes.SINGLETON)
    setOf[PartitionMount].add[GitPartitionMounter].in(Scopes.SINGLETON)

    bind[DefaultRepositoryStore].in(Scopes.SINGLETON)
    setOf[RepositoryStore].add[XmlRepositoryStore].in(Scopes.SINGLETON)
    setOf[Realm].add[GitOwnerRealm].in(Scopes.SINGLETON)
    setOf[PermissionResolver].add[GitPermissionResolver].in(Scopes.SINGLETON)
    setOf[ResourceSetStore].add[GitrResourcePatterns].in(Scopes.SINGLETON)
  }

  @Provides@Singleton
  def createGitrManager(config: Config): GitrMan = new GitrManImpl(config.repositories)

  @Provides@Singleton@Named("contentroot")
  def createMainPartition(gitr: GitPartMan): Container =
    gitr.getOrCreate(contentRootRepo, org.eknet.publet.gitr.partition.Config(None))

  val name = "Git Module"
}

@Singleton
class GitrResourcePatterns @Inject() (config: Config) extends ResourceSetStoreAdapter {
  override def anonPatterns = List(GitrControl.mountPoint+"/**")
}