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
import org.eknet.publet.auth.store.{DefaultAuthStore, UserProperty}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.07.12 17:00
 */
class DigestCredentialsMatcher(authm: DefaultAuthStore) extends CredentialsMatcher {

  def doCredentialsMatch(token: AuthenticationToken, info: AuthenticationInfo) = {
    if (token.isInstanceOf[DigestAuthenticationToken]) {
      matchDigest(token.asInstanceOf[DigestAuthenticationToken])
    } else {
      false
    }
  }

  private def matchDigest(token: DigestAuthenticationToken) = {
    val digestResp = token.getCredentials
    val user = authm.findUser(token.getPrincipal)
    if (user.isDefined) {
      val dig = DigestGenerator.generateDigestWithEncryptedPassword(digestResp, user.get.get(UserProperty.digest).getOrElse(""))
      digestResp.responseDigest == dig
    } else {
      false
    }
  }
}

class DigestAuthenticationToken(digestResponse: DigestResponse) extends AuthenticationToken {
  def getPrincipal = digestResponse.user
  def getCredentials = digestResponse
}
