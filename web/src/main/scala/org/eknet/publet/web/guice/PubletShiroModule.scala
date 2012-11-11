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
import org.apache.shiro.realm.Realm
import org.apache.shiro.web.mgt.{WebSecurityManager, DefaultWebSecurityManager}
import com.google.inject.binder.AnnotatedBindingBuilder
import java.util
import org.apache.shiro.web.filter.mgt.{FilterChainResolver, PathMatchingFilterChainResolver}
import org.apache.shiro.web.filter.authc.{AnonymousFilter, BasicHttpAuthenticationFilter, FormAuthenticationFilter}
import org.eknet.publet.web.{Settings, Config}
import javax.servlet.ServletContext
import org.apache.shiro.web.env.WebEnvironment
import org.eknet.publet.web.util.StringMap
import org.apache.shiro.session.mgt.SessionManager
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager
import com.google.common.eventbus.EventBus
import org.apache.shiro.authc.{AuthenticationListener, AbstractAuthenticator}
import org.apache.shiro.cache.CacheManager
import org.eknet.guice.squire.SquireModule
import org.eknet.publet.auth.store.{UserStore, PermissionStore, ResourceSetStore}
import org.eknet.publet.web.shiro.SuperadminRealm

/**
 * Needs services defined in [[org.eknet.publet.web.guice.AppModule]]
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.10.12 14:02
 */
class PubletShiroModule extends SquireModule with PubletBinding {

  def configure() {
    bind[SessionManager].to[ServletContainerSessionManager].asEagerSingleton()
    bind[WebEnvironment].to[GuiceWebEnvironment].asEagerSingleton()

    setOf[ResourceSetStore].add[DefaultResourcePatterns].in(Scopes.SINGLETON)

    setOf[Realm].add[SuperadminRealm].in(Scopes.SINGLETON)
  }

  @Provides@Singleton
  def createWebSecurityManager(bus: EventBus, cacheMan: CacheManager, realms: util.Set[Realm], listeners: util.Set[AuthenticationListener]): WebSecurityManager = {
    val sm = new DefaultWebSecurityManager()
    sm.setRealms(realms)
    sm.setCacheManager(cacheMan)
    sm.getAuthenticator.asInstanceOf[AbstractAuthenticator].setAuthenticationListeners(listeners)
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
  def getLoginPath(settings: Settings) =
    settings("publet.loginUrl").getOrElse("/publet/templates/login.html")


  def bindSecurityManager(bind: AnnotatedBindingBuilder[_ >: WebSecurityManager]) {
    bind.toConstructor(classOf[DefaultWebSecurityManager].getConstructor(classOf[util.Collection[_]])).asEagerSingleton()
  }
}