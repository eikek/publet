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

package org.eknet.publet.ext.counter

import com.google.inject.{Scopes, AbstractModule}
import org.eknet.publet.web.guice.{PubletBinding, PubletModule}
import org.eknet.publet.web.WebExtension

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.10.12 02:48
 */
class CounterModule extends AbstractModule with PubletModule with PubletBinding {
  def configure() {
    bind(classOf[CounterService]) to (classOf[CounterServiceImpl]) in Scopes.SINGLETON
    binder.bindExtension.toType[CounterExtension]
  }
}
