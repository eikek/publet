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

package org.eknet.publet.web.template

import org.fusesource.scalate.util.{Resource, ResourceLoader}
import org.eknet.publet.vfs.ContentResource
import org.eknet.publet.engine.scalate.TemplateResource
import org.eknet.publet.web.{Config, PubletWeb}

/**
 * Scalate resource loader that looks up "special resources"
 * located in `.includes` or `.allIncludes`.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.07.12 23:24
 */
class IncludeResourceLoader(delegate: ResourceLoader, config: Config) extends ResourceLoader {

  private val includeLoader = new IncludeLoader(config)
  private val specialPattern = """.*/__incl_([^/]*)$""".r

  def resource(uri: String): Option[Resource] = {
    delegate.resource(uri) orElse specialResource(uri)
  }

  private def specialResource(uri: String) = {
    uri match {
      case specialPattern(r) => includeLoader.findInclude(r)
        .flatMap(p => PubletWeb.publet.rootContainer.lookup(p))
        .collect({ case c: ContentResource => c })
        .map(cr => new TemplateResource(uri, cr))
      case _ => None
    }
  }
}
