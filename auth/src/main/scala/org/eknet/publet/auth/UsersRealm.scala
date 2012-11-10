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

package org.eknet.publet.auth

import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.authc.{UsernamePasswordToken, AuthenticationToken}
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher
import org.eknet.publet.auth.store.DefaultAuthStore
import com.google.inject.{Singleton, Inject}
import com.google.common.eventbus.Subscribe
import org.apache.shiro.cache.MemoryConstrainedCacheManager
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 08:14
 */
@Singleton
class UsersRealm @Inject() (val db: DefaultAuthStore, resolver: DefaultPermissionResolver) extends AuthorizingRealm with Logging {

  @Subscribe
  def clearCacheOnChange(event: AuthDataChanged) {
    Option(getAuthenticationCache).map(_.clear())
    Option(getAuthorizationCache).map(_.clear())
  }

  setCredentialsMatcher(new CompositeCredentialsMatcher(List(
    new DynamicHashCredentialsMatcher,
    new DigestCredentialsMatcher,
    new SimpleCredentialsMatcher
  )))

  setPermissionResolver(resolver)
  setCacheManager(new MemoryConstrainedCacheManager)

  override def supports(token: AuthenticationToken) = {
    token.isInstanceOf[DigestAuthenticationToken] || token.isInstanceOf[UsernamePasswordToken]
  }

  def doGetAuthenticationInfo(token: AuthenticationToken) = {
    val user = token.getPrincipal.toString
    db.findUser(user).map(u => new UserAuthcInfo(u)).orNull
  }

  def doGetAuthorizationInfo(principals: PrincipalCollection): AuthorizationInfo = {
    val login = getAvailablePrincipal(principals).toString
    db.findUser(login).map(u => {
      val groups = db.getGroups(login)
      val dbperms = db.getAllUserPermissions(login)
      new PolicyAuthzInfo(u, groups, dbperms)
    }).orNull
  }

}
