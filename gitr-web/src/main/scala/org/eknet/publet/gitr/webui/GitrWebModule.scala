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

package org.eknet.publet.gitr.webui

import com.google.inject.AbstractModule
import org.eknet.publet.web.guice.{PubletModule, PubletBinding}
import org.eknet.guice.squire.SquireModule
import org.eknet.publet.vfs.Resource

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 15.10.12 19:48
 */
class GitrWebModule extends SquireModule with PubletBinding with PubletModule {

  def configure() {
    bind[GitrWebExtension].asEagerSingleton()
    bindDocumentation(docResource("_gitrweb.md", "gitr-shot1.png", "gitr-shot2.png"))
  }

  private[this] def docResource(names: String*) = names.map("org/eknet/publet/gitr/doc/"+ _).map(Resource.classpath(_)).toList

  override def toString = "Git Web Frontend"
}
