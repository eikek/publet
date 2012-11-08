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

package org.eknet.publet.web.guice

import org.apache.shiro.realm.Realm
import org.eknet.publet.web.WebExtension
import org.eknet.publet.web.req.RequestHandlerFactory
import org.eknet.publet.auth.store.UserStore
import org.eknet.guice.squire.SquireBinder

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 15.10.12 13:03
 * 
 */
trait PubletBinding extends SquireBinder {

  def bindRequestHandler = setOf[RequestHandlerFactory]
  def bindExtension = setOf[WebExtension]
  def bindRealm = setOf[Realm]
  def bindUserStore = setOf[UserStore]

}


