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

import org.scalatest.BeforeAndAfter
import org.apache.shiro.mgt.{DefaultSecurityManager, SecurityManager}
import org.apache.shiro.subject.{PrincipalCollection, Subject}
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.{AuthenticationToken, SimpleAccount, UsernamePasswordToken}
import org.apache.shiro.realm.AuthorizingRealm

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 08.11.12 12:13
 */
trait SecurityManagerMock {
  this: BeforeAndAfter =>

  var securityManager: SecurityManager = _
  var subject: Subject = _

  before {
    securityManager = new DefaultSecurityManager(new TestRealm)
    SecurityUtils.setSecurityManager(securityManager)
    subject = SecurityUtils.getSubject
    subject.login(new UsernamePasswordToken("test", "test"))
  }


  class TestRealm extends AuthorizingRealm {
    import collection.JavaConversions._

    setPermissionResolver(new ResourcePermissionResolver)
    def account = {
      val acc = new SimpleAccount("test", "test", "test")
      acc.setRoles(Set("manager", "developer"))
      acc.setStringPermissions(Set("resource:read:/aa/bb/cc/**",
        "resource:*:/aa/uu/**",
        "resource:read,delete:/cc/*/d/**,/cc/*/e/**"))
      acc
    }

    def doGetAuthenticationInfo(token: AuthenticationToken) = account
    def doGetAuthorizationInfo(principals: PrincipalCollection) = account
  }
}
