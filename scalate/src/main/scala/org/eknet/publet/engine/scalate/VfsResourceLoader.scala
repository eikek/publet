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

package org.eknet.publet.engine.scalate

import org.eknet.publet.Publet
import org.fusesource.scalate.util.ResourceLoader
import org.eknet.publet.vfs.{Container, ContentResource, Path}
import org.fusesource.scalate.TemplateEngine

/**
 * A resource loader that uses [[org.eknet.publet.vfs.Container]] to lookup
 * the uri if it fails using the delegate.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 00:54
 */
class VfsResourceLoader(container: Container, delegate: Option[ResourceLoader]) extends ResourceLoader {

  def this(publet: Publet, resourceLoader: ResourceLoader) = this(publet.rootContainer, Some(resourceLoader))

  def resource(uri: String) = {
    delegate.flatMap(_.resource(uri)) orElse {
      container.lookup(Path(uri))
        .collect({ case c:ContentResource => new TemplateResource(uri, c)})
    }
  }
}

object VfsResourceLoader {

  def install(engine: TemplateEngine, publet: Publet) {
    val vfsl = new VfsResourceLoader(publet, engine.resourceLoader)
    engine.resourceLoader = vfsl
  }
}
