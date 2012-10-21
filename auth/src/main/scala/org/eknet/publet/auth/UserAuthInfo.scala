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

import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.subject.SimplePrincipalCollection

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.10.12 23:40
 */
final case class UserAuthInfo(user: User) extends AuthenticationInfo {
  def getPrincipals = new SimplePrincipalCollection(user.login, "Publet Protected")
  def getCredentials = user.password
  def algorithm = user.algorithm
}
