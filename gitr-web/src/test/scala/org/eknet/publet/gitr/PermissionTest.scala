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

package org.eknet.publet.gitr

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSuite, BeforeAndAfter}
import org.apache.shiro.mgt.{DefaultSecurityManager, SecurityManager}
import org.apache.shiro.subject.{PrincipalCollection, Subject}
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.{AuthenticationToken, SimpleAccount, UsernamePasswordToken}
import org.apache.shiro.realm.AuthorizingRealm
import org.eknet.publet.auth.ResourcePermissionResolver

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.11.12 19:29
 */
class PermissionTest extends FunSuite with ShouldMatchers with BeforeAndAfter {
  import collection.JavaConversions._

  var securityManager: SecurityManager = _
  var subject: Subject = _

  before {
    securityManager = new DefaultSecurityManager(new TestRealm)
    SecurityUtils.setSecurityManager(securityManager)
    subject = SecurityUtils.getSubject
    subject.login(new UsernamePasswordToken("test", "test"))
  }

  test ("git push implies") {

    subject.isPermitted("git:pull:repo1") should be (true)
    subject.isPermitted("git:push:repo1") should be (true)
    subject.isPermitted("git:push:repo2") should be (false)
    subject.isPermitted("git:pull:repo2") should be (true)

  }

  class TestRealm extends AuthorizingRealm {
    setPermissionResolver(new ResourcePermissionResolver)
    def account = {
      val acc = new SimpleAccount("test", "test", "test")
      acc.setRoles(Set("manager", "developer"))
      acc.setStringPermissions(Set("git:push:repo1",
        "git:pull:repo2"))
      acc
    }

    def doGetAuthenticationInfo(token: AuthenticationToken) = account
    def doGetAuthorizationInfo(principals: PrincipalCollection) = account
  }

}
