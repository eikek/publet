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
import org.eknet.publet.web.guice.{AbstractPubletModule, PubletBinding, PubletModule}
import org.eknet.publet.web.WebExtension
import org.eknet.guice.squire.SquireModule
import org.eknet.publet.vfs.{ContentResource, Resource}
import org.eknet.publet.vfs.util.UrlResource
import com.google.inject.name.Names
import collection.JavaConversions._
import com.google.inject.multibindings.MapBinder
import org.eknet.publet.web.util.AppSignature

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.10.12 02:48
 */
class CounterModule extends AbstractPubletModule with PubletModule with PubletBinding {

  def configure() {
    bind[CounterService].to[CounterServiceImpl].in(Scopes.SINGLETON)
    bindExtension.add[CounterExtension]

    bindDocumentation(List(Resource.classpath("org/eknet/publet/ext/doc/counterdoc.md")))
  }

  val name = "Page Counter"
}
