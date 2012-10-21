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

import org.apache.shiro.authc.credential.CredentialsMatcher
import org.apache.shiro.authc.{UsernamePasswordToken, AuthenticationInfo, AuthenticationToken}
import java.lang.String

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.10.12 23:39
 */
class DynamicHashCredentialsMatcher extends CredentialsMatcher {

  def doCredentialsMatch(token: AuthenticationToken, info: AuthenticationInfo) = {
    (info, token) match {
      case (ui: UserAuthInfo, pwt: UsernamePasswordToken) => {
        ui.algorithm.map(PasswordServiceProvider.newPasswordService(_)) match {
          case Some(ps) => ps.passwordsMatch(token.getCredentials, new String(ui.getCredentials))
          case _ => false
        }
      }
      case _ => false
    }
  }
}
