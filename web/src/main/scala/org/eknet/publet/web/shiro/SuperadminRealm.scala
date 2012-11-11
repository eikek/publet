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

package org.eknet.publet.web.shiro

import org.eknet.publet.auth.store.{PermissionStore, UserProperty, User, UserStoreAdapter}
import org.eknet.publet.web.{ConfigReloadedEvent, Config}
import com.google.inject.{Singleton, Inject}
import org.eknet.publet.auth._
import org.apache.shiro.realm.text.TextConfigurationRealm
import com.google.common.eventbus.Subscribe
import org.apache.shiro.authc.{SimpleAccount, UsernamePasswordToken, AuthenticationInfo, AuthenticationToken}
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher
import org.apache.shiro.subject.PrincipalCollection
import org.eknet.publet.web.ConfigReloadedEvent
import org.apache.shiro.realm.AuthorizingRealm

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 08.11.12 18:44
 */
@Singleton
class SuperadminRealm @Inject() (config: Config) extends AuthorizingRealm {

  private val login = "superadmin"

  private var user = {
    val pw = config("superadminPassword").getOrElse(login)
    new User(login, Map(
      UserProperty.password -> pw,
      UserProperty.enabled -> config("superadminEnabled").getOrElse("true"),
      UserProperty.digest -> DigestGenerator.encodePasswordInA1Format(login, "WebDav Area", pw),
      UserProperty.fullName -> "Publet Superadmin"
    ))
  }

  setCredentialsMatcher(new CompositeCredentialsMatcher(List(
    new DynamicHashCredentialsMatcher,
    new DigestCredentialsMatcher,
    new SimpleCredentialsMatcher
  )))
  setCachingEnabled(false)


  @Subscribe
  def reloadRealm(event: ConfigReloadedEvent) {
    val map = this.user.properties + (UserProperty.enabled -> config("superadminEnabled").getOrElse("true"))
    this.user = new User(user.login, map)
  }

  override def supports(token: AuthenticationToken) =
    super.supports(token) || (token != null && token.isInstanceOf[DigestAuthenticationToken])

  def doGetAuthenticationInfo(token: AuthenticationToken) = {
    if (token.getPrincipal.toString == login) {
      new UserAuthcInfo(user, getName)
    } else {
      null
    }
  }

  def doGetAuthorizationInfo(principals: PrincipalCollection) = {
    val principal = getAvailablePrincipal(principals).toString
    if (principal == login) {
      new PolicyAuthzInfo(user, Set(), Set("*"))
    } else {
      null
    }
  }
}
