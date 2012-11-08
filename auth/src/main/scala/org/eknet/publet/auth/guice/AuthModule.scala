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

package org.eknet.publet.auth.guice

import com.google.inject.{Singleton, Provides, Scopes}
import org.eknet.publet.auth._
import org.eknet.guice.squire.SquireModule
import org.apache.shiro.cache.{MemoryConstrainedCacheManager, CacheManager}
import org.apache.shiro.realm.Realm
import com.google.inject.name.Named
import org.apache.shiro.crypto.hash._
import org.apache.shiro.authc.credential.PasswordService
import org.apache.shiro.authz.permission.PermissionResolver
import org.eknet.publet.auth.store.{DefaultAuthStore, ResourceSetStore, PermissionStore, UserStore}
import org.eknet.publet.auth.xml.XmlDatabase
import org.apache.shiro.authc.AuthenticationListener

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 01.11.12 19:10
 */
class AuthModule extends SquireModule {

  def configure() {
    bind[PasswordServiceProvider].to[DefaultPasswordServiceProvider].as[Singleton]()
    bind[CacheManager].to[MemoryConstrainedCacheManager].in(Scopes.SINGLETON)

    bind[DefaultAuthStore] in Scopes.SINGLETON

    bind[PermissionResolver].to[DefaultPermissionResolver]
    setOf[Realm].add[UsersRealm]
    setOf[PermissionResolver].add[ResourcePermissionResolver]

    bind[XmlDatabase].in(Scopes.SINGLETON)
    setOf[UserStore].add[XmlDatabase]
    setOf[PermissionStore].add[XmlDatabase]
    setOf[ResourceSetStore].add[XmlDatabase]

    setOf[AuthenticationListener].add[AuthListener].in(Scopes.SINGLETON)
  }

  @Singleton
  @Provides
  @Named(Sha512Hash.ALGORITHM_NAME)
  def createSha512PasswordService(): PasswordService =
    PasswordServiceProvider.newPasswordService(Sha512Hash.ALGORITHM_NAME)

  @Provides@Singleton@Named(Md5Hash.ALGORITHM_NAME)
  def createMd5PasswordService(): PasswordService =
    PasswordServiceProvider.newPasswordService(Md5Hash.ALGORITHM_NAME)

  @Provides@Singleton@Named(Md2Hash.ALGORITHM_NAME)
  def createMd2PasswordService(): PasswordService =
    PasswordServiceProvider.newPasswordService(Md2Hash.ALGORITHM_NAME)

  @Provides@Singleton@Named(Sha1Hash.ALGORITHM_NAME)
  def createSha1PasswordService(): PasswordService =
    PasswordServiceProvider.newPasswordService(Sha1Hash.ALGORITHM_NAME)

  @Provides@Singleton@Named(Sha384Hash.ALGORITHM_NAME)
  def createSha384PasswordService(): PasswordService =
    PasswordServiceProvider.newPasswordService(Sha384Hash.ALGORITHM_NAME)


}
