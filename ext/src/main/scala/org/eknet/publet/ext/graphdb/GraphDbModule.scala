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

package org.eknet.publet.ext.graphdb

import com.google.inject._
import org.eknet.publet.web.guice.{PubletModule, PubletBinding}
import org.eknet.guice.squire.SquireModule
import org.eknet.publet.vfs.{Resource, ContentResource}
import com.google.inject.name.Names

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 11.10.12 22:43
 */
class GraphDbModule extends SquireModule with PubletBinding with PubletModule {

  def configure() {
    bind[GraphDbProvider].to[DefaultGraphDbProvider] in Scopes.SINGLETON
    bind[GraphDbShutdownHook].asEagerSingleton()

    annoateMapOf[Class[_], List[ContentResource]]
      .by(Names.named("ExtDoc"))
      .add(classOf[GraphDbModule])
      .toInstance(List(Resource.classpath("org/eknet/publet/ext/doc/graphdbdoc.md")))
  }

}
