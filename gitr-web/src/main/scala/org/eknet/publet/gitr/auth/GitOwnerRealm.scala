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

package org.eknet.publet.gitr.auth

import org.eknet.publet.auth.store.{User, PermissionStore, UserStore}
import org.eknet.publet.auth.PermissionBuilder._
import org.eknet.publet.auth.{AuthDataChanged, PermissionBuilder}
import com.google.inject.{Inject, Singleton}
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authz.SimpleAuthorizationInfo
import com.google.common.eventbus.Subscribe

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.11.12 22:47
 */
@Singleton
class GitOwnerRealm @Inject() (db: DefaultRepositoryStore) extends AuthorizingRealm with GitPermissionBuilder {
  import GitAction._

  @Subscribe
  def clearCacheOnChange(event: AuthDataChanged) {
    Option(getAuthenticationCache).map(_.clear())
    Option(getAuthorizationCache).map(_.clear())
  }

  def doGetAuthenticationInfo(token: AuthenticationToken) = null

  def doGetAuthorizationInfo(principals: PrincipalCollection) = {
    val login = getAvailablePrincipal(principals).toString
    val info = new SimpleAuthorizationInfo()
    for (rm <- db.allRepositories if (rm.owner == login)) {
      info.addStringPermission(git.action(pull,push,admin) on rm.name)
    }
    info
  }

}
