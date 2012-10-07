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
import org.eknet.publet.web.shiro.UsersRealm
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

/**
 * Needs services defined in [[org.eknet.publet.web.guice.PubletModule]]
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.10.12 14:02
 */
object PubletShiroModule extends AbstractModule {

  def configure() {
    bind(classOf[CacheManager]) to classOf[MemoryConstrainedCacheManager] in Scopes.SINGLETON
    bind(classOf[SessionManager]) to classOf[ServletContainerSessionManager] asEagerSingleton()
    bindRealm.to(classOf[UsersRealm])

    bind(classOf[WebEnvironment]).to(classOf[GuiceWebEnvironment]).asEagerSingleton()
  }

  @Provides@Singleton
  def createWebSecurityManager(cacheMan: CacheManager, realms: util.Set[Realm]): WebSecurityManager = {
    val sm = new DefaultWebSecurityManager()
    sm.setRealms(realms)
    sm.setCacheManager(cacheMan)
    sm
  }

  @Provides@Singleton
  def createFilterChainResolver(@Named("publetServletContext") servletContext: ServletContext, @Named("loginPath") loginPath: String): FilterChainResolver = {
    val resolver = new PathMatchingFilterChainResolver()
    val formauth = new FormAuthenticationFilter()
    val loginUrl = Config("publet.urlBase").getOrElse(servletContext.getContextPath) + loginPath
    formauth.setLoginUrl(loginUrl)
    resolver.getFilterChainManager.addFilter("authc", formauth)
    resolver.getFilterChainManager.addFilter("authcBasic", new BasicHttpAuthenticationFilter)
    resolver.getFilterChainManager.addFilter("anon", new AnonymousFilter)

    val gitPath = Path(Config.gitMount).toAbsolute.asString + "/**"
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


  def bindRealm = {
    val multibinder = Multibinder.newSetBinder(binder, classOf[Realm])
    multibinder.addBinding()
  }

  def bindSecurityManager(bind: AnnotatedBindingBuilder[_ >: WebSecurityManager]) {
    bind.toConstructor(classOf[DefaultWebSecurityManager].getConstructor(classOf[util.Collection[_]])).asEagerSingleton()
  }
}
