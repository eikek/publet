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

package org.eknet.publet.web.filter

import org.apache.shiro.web.servlet.AbstractShiroFilter
import org.apache.shiro.web.env.WebEnvironment
import org.apache.shiro.web.util.WebUtils
import org.apache.shiro.web.filter.mgt.FilterChainResolver
import org.eknet.publet.auth.RepositoryTag
import org.eknet.publet.web.{PubletWebContext, GitAction, PubletWeb}
import org.eknet.publet.web.util.{Key, Request}
import javax.servlet.{ServletResponse, ServletRequest}

/**
 * The [[org.apache.shiro.web.servlet.ShiroFilter]] is usually configured
 * statically from a configuration (ini) file.
 *
 * Here, the request's authorization depends on dynamically configured
 * settings. A repository may become open or closed at any time. Therefore
 * a check on each request is necessary to determine if it must be
 * authenticated and authorized or not.
 *
 * Shiro's web environment is setup on startup as usual and configured
 * to intercept every url. It defines a BASIC authentication filter on the
 * path where the git filter is listening and a form-login authentication
 * filter on all other paths. This filter then decides based on the request
 * whether to skip authentication at all.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 13:19
 */
class PubletShiroFilter extends AbstractShiroFilter with HttpFilter {

  override def init() {
    val env: WebEnvironment = WebUtils.getRequiredWebEnvironment(getServletContext)
    setSecurityManager(env.getWebSecurityManager)

    val resolver: FilterChainResolver = env.getFilterChainResolver
    if (resolver != null) {
      setFilterChainResolver(resolver)
    }
  }

  override def isEnabled(request: ServletRequest, response: ServletResponse) = {
    //disable filter if this is a read request to an open repository. otherwise
    //activate filter
    PubletShiroFilter.shiroFilterEnabled
  }

}

object PubletShiroFilter {

  def anonRequestAllowed = PubletWebContext.attr(anonRequestKey).get

  def shiroFilterEnabled = PubletWebContext.attr(shiroEnabledKey).get

  private val anonRequestKey = Key("anonymousRequestAllowed", {
    case Request => {
      val action = PubletWebContext.getGitAction
      val tag = PubletWebContext.getRepositoryModel
        .map(_.tag)
        .getOrElse(RepositoryTag.open)

      val anon = tag == RepositoryTag.open && action.exists(_ == GitAction.pull)

      if (!anon && !PubletWebContext.isGitRequest) {
        val constr = PubletWeb.authManager.getResourceConstraints(PubletWebContext.applicationUri)
        constr.find(_.perm.isAnon).isDefined
      } else {
        anon
      }
    }
  })

  private val shiroEnabledKey = Key("shiroOnRequestEnabled", {
    case Request => {
      val anonAllowed = anonRequestAllowed
      if (PubletWebContext.isGitRequest && anonAllowed)
        false
      else
        true
    }
  })
}