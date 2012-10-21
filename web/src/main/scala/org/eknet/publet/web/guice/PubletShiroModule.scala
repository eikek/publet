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

package org.eknet.publet.web.guice

import com.google.inject._
import name.Named
import org.eknet.publet.web.shiro.{AuthListener, UsersRealm}
import org.apache.shiro.realm.Realm
import com.google.inject.multibindings.Multibinder
import org.apache.shiro.cache.{MemoryConstrainedCacheManager, CacheManager}
import org.apache.shiro.web.mgt.{WebSecurityManager, DefaultWebSecurityManager}
import com.google.inject.binder.AnnotatedBindingBuilder
import java.util
import org.apache.shiro.web.filter.mgt.{FilterChainResolver, PathMatchingFilterChainResolver}
import org.apache.shiro.web.filter.authc.{AnonymousFilter, BasicHttpAuthenticationFilter, FormAuthenticationFilter}
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.Config
import javax.servlet.ServletContext
import org.apache.shiro.web.env.WebEnvironment
import org.eknet.publet.web.util.StringMap
import org.apache.shiro.session.mgt.SessionManager
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager
import org.apache.shiro.authc.credential.{DefaultPasswordService, PasswordService}
import org.apache.shiro.crypto.hash._
import org.apache.shiro.crypto.hash.format.{Shiro1CryptFormat}
import com.google.common.eventbus.EventBus
import org.apache.shiro.authc.AbstractAuthenticator
import org.eknet.publet.auth.{DefaultPasswordServiceProvider, PasswordServiceProvider}

/**
 * Needs services defined in [[org.eknet.publet.web.guice.AppModule]]
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.10.12 14:02
 */
object PubletShiroModule extends AbstractModule with PubletBinding {

  def configure() {
    binder.set[PasswordServiceProvider].toType[DefaultPasswordServiceProvider] in  Scopes.SINGLETON
    binder.set[CacheManager].toType[MemoryConstrainedCacheManager] in Scopes.SINGLETON
    binder.set[SessionManager].toType[ServletContainerSessionManager] asEagerSingleton()
    binder.bindRealm.toType[UsersRealm]

    binder.set[WebEnvironment].toType[GuiceWebEnvironment].asEagerSingleton()
  }

  @Provides@Singleton@Named(Sha512Hash.ALGORITHM_NAME)
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


  @Provides@Singleton
  def createWebSecurityManager(bus: EventBus, cacheMan: CacheManager, realms: util.Set[Realm]): WebSecurityManager = {
    import collection.JavaConversions._
    val sm = new DefaultWebSecurityManager()
    sm.setRealms(realms)
    sm.setCacheManager(cacheMan)
    sm.getAuthenticator.asInstanceOf[AbstractAuthenticator].setAuthenticationListeners(List(new AuthListener(bus)))
    sm
  }

  @Provides@Singleton
  def createFilterChainResolver(@Named("publetServletContext") servletContext: ServletContext, @Named("loginPath") loginPath: String, config: Config): FilterChainResolver = {
    val resolver = new PathMatchingFilterChainResolver()
    val formauth = new FormAuthenticationFilter()
    val loginUrl = config("publet.urlBase").getOrElse(servletContext.getContextPath) + loginPath
    formauth.setLoginUrl(loginUrl)
    resolver.getFilterChainManager.addFilter("authc", formauth)
    resolver.getFilterChainManager.addFilter("authcBasic", new BasicHttpAuthenticationFilter)
    resolver.getFilterChainManager.addFilter("anon", new AnonymousFilter)

    val gitPath = Path(config.gitMount).toAbsolute.asString + "/**"
    resolver.getFilterChainManager.createChain(gitPath, "authcBasic")
    resolver.getFilterChainManager.createChain("/**", "anon")
    resolver
  }

  /**
   * Returns the uri to the login page. This is the inner-application
   * uri. Use `urlOf()` to create a URL.
   *
   * @return
   */
  @Provides@Singleton@Named("loginPath")
  def getLoginPath(@Named("settings") settings: StringMap) =
    settings("publet.loginUrl").getOrElse("/publet/templates/login.html")


  def bindSecurityManager(bind: AnnotatedBindingBuilder[_ >: WebSecurityManager]) {
    bind.toConstructor(classOf[DefaultWebSecurityManager].getConstructor(classOf[util.Collection[_]])).asEagerSingleton()
  }
}
