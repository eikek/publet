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

import org.apache.shiro.web.env.{EnvironmentLoader, WebEnvironment}
import javax.inject.Singleton
import org.apache.shiro.web.filter.mgt.FilterChainResolver
import com.google.inject.Inject
import javax.servlet.ServletContext
import org.apache.shiro.web.mgt.WebSecurityManager
import com.google.inject.name.Named

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.10.12 14:27
 */
@Singleton
class GuiceWebEnvironment @Inject() (
                          filterChainResolver: FilterChainResolver,
                          @Named("publetServletContext") servletContext: ServletContext,
                          securityManager: WebSecurityManager) extends WebEnvironment {

  //set this into the servlet context
  servletContext.setAttribute(EnvironmentLoader.ENVIRONMENT_ATTRIBUTE_KEY, this)

  def getFilterChainResolver = filterChainResolver
  def getServletContext = servletContext
  def getWebSecurityManager = securityManager
  def getSecurityManager = securityManager
}
