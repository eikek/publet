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
import org.apache.shiro.authc.{AuthenticationInfo, AuthenticationToken}
import scala.annotation.tailrec

/**
 * Uses a list of matchers combining them or-like. That means at least one of the matchers
 * must return true for a successful match.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.10.12 23:41
 */
class CompositeCredentialsMatcher(matchers: List[CredentialsMatcher]) extends CredentialsMatcher {

  @tailrec
  private final def executeMatchers(matchers: List[CredentialsMatcher], token: AuthenticationToken, info: AuthenticationInfo): Boolean = {
    matchers match {
      case c::cs => {
        if (c.doCredentialsMatch(token, info)) true
        else executeMatchers(cs, token, info)
      }
      case _ => false
    }
  }

  def doCredentialsMatch(token: AuthenticationToken, info: AuthenticationInfo) = {
    executeMatchers(matchers, token, info)
  }
}
