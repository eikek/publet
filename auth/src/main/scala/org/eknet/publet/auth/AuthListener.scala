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

import com.google.common.eventbus.EventBus
import org.apache.shiro.authc.{AuthenticationInfo, AuthenticationException, AuthenticationToken, AuthenticationListener}
import org.apache.shiro.subject.PrincipalCollection
import org.eknet.publet.event.Event
import com.google.inject.{Inject, Singleton}

/**
 * Is registered on the [[org.apache.shiro.mgt.SecurityManager]] and posts all
 * events on the global event bus.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 11.10.12 00:56
 */
@Singleton
class AuthListener @Inject() (bus:EventBus) extends AuthenticationListener {
  def onSuccess(token: AuthenticationToken, info: AuthenticationInfo) {
    bus.post(new AuthSuccessEvent(token, info))
  }

  def onFailure(token: AuthenticationToken, ae: AuthenticationException) {
    bus.post(new AuthFailedEvent(token, ae))
  }

  def onLogout(principals: PrincipalCollection) {
    bus.post(new LogoutEvent(principals))
  }
}

abstract sealed class AuthEvent extends Event
case class AuthFailedEvent(token: AuthenticationToken, ae: AuthenticationException) extends AuthEvent
case class AuthSuccessEvent(token: AuthenticationToken, info: AuthenticationInfo) extends AuthEvent
case class LogoutEvent(principals: PrincipalCollection)
