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

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.apache.shiro.mgt.{DefaultSecurityManager, SecurityManager}
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.{Subject, PrincipalCollection}
import org.apache.shiro.authc.{UsernamePasswordToken, SimpleAccount, AuthenticationToken}
import org.apache.shiro.authz.{Permission => ShiroPermission}
import org.apache.shiro.SecurityUtils

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.11.12 19:46
 */
class PermissionTest extends FunSuite with ShouldMatchers with BeforeAndAfter with SecurityManagerMock {

  test ("resource implies") {
    subject.isPermitted("resource:write:/aa/uu/test.pdf") should be (true)
    subject.isPermitted("resource:read:/aa/bb/test.pdf") should be (false)
    subject.isPermitted("resource:read,create:/aa/uu/admiral.pdf") should be (true)

    subject.isPermitted("resource:delete:/cc/a/d/doc.pdf,/cc/b/d/doc.pdf,/cc/y/e/zonk.xml") should be (true)
    subject.isPermitted("resource:write:/cc/a/d/doc.pdf,/cc/b/d/doc.pdf,/cc/y/e/zonk.xml") should be (false)
  }

}
