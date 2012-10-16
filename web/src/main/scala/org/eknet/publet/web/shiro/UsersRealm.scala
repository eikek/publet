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

import collection.JavaConversions._
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.authz.{SimpleAuthorizationInfo, AuthorizationInfo}
import org.apache.shiro.subject.{SimplePrincipalCollection, PrincipalCollection}
import org.apache.shiro.authc.{DisabledAccountException, AuthenticationInfo, AuthenticationToken}
import org.apache.shiro.SecurityUtils
import org.eknet.publet.auth.{PubletAuth, Policy, User}
import org.apache.shiro.authc.credential.{SimpleCredentialsMatcher, CredentialsMatcher}
import com.google.inject.{Singleton, Inject}
import org.eknet.publet.web.guice.PubletShiroModule
import com.google.common.eventbus.EventBus

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 08:14
 */
@Singleton
class UsersRealm @Inject() (val db: PubletAuth, bus: EventBus) extends AuthorizingRealm {

  setCredentialsMatcher(new DynamicHashCredentialsMatcher())

  override def assertCredentialsMatch(token: AuthenticationToken, info: AuthenticationInfo) {
    super.assertCredentialsMatch(token, info)
  }

  def doGetAuthenticationInfo(token: AuthenticationToken) = {
    val user = token.getPrincipal.toString
    db.findUser(user) match {
      case Some(u) => {
        if (u.isEnabled) new UserAuthInfo(u)
        else throw new DisabledAccountException("Account disabled.")
      }
      case None => null
    }
  }

  def doGetAuthorizationInfo(principals: PrincipalCollection) = {
    val login = principals.getPrimaryPrincipal.toString
    db.findUser(login) map { user =>
      new PolicyAuthInfo(user)
    } getOrElse {
      new SimpleAuthorizationInfo()
    }
  }

  class PolicyAuthInfo(user: User) extends AuthorizationInfo {
    def getRoles = user.groups

    private def policy = {
      val op = Option(Security.session.getAttribute("policy")).map(_.asInstanceOf[Policy])
      op.getOrElse {
        val policy = db.getPolicy(user)
        SecurityUtils.getSubject.getSession.setAttribute("policy", policy)
        policy
      }
    }

    def getStringPermissions = policy.getPermissions
    def getObjectPermissions = List()
  }

  class UserAuthInfo(user: User) extends AuthenticationInfo {
    def getPrincipals = new SimplePrincipalCollection(user.login, "Publet Protected")
    def getCredentials = user.password
    def algorithm = user.algorithm
  }

  class DynamicHashCredentialsMatcher extends CredentialsMatcher {
    private val fallback = new SimpleCredentialsMatcher()

    def doCredentialsMatch(token: AuthenticationToken, info: AuthenticationInfo) = {
      info match {
        case ui: UserAuthInfo => {
          ui.algorithm.map(PubletShiroModule.newPasswordService(_)) match {
            case Some(ps) => ps.passwordsMatch(token.getCredentials, new String(ui.getCredentials))
            case _ => fallback.doCredentialsMatch(token, info)
          }
        }
        case _ => fallback.doCredentialsMatch(token, info)
      }
    }
  }

}
