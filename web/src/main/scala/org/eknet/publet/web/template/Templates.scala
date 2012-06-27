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

import org.eknet.publet.Publet
import org.eknet.publet.vfs.util.ClasspathContainer
import org.eknet.publet.vfs.Path
import Path._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 19:04
 */
object Templates {

  def mountJQuery(publet: Publet) {
    publet.mountManager.mount("/publet/jquery/".p,
      new ClasspathContainer(base ="/org/eknet/publet/web/includes/jquery"))
  }

  def mountHighlightJs(publet: Publet) {
    publet.mountManager.mount("/publet/highlightjs/".p,
      new ClasspathContainer(base = "/org/eknet/publet/web/includes/highlight"))
  }

  def mountPubletResources(publet: Publet) {
    val includes = new ClasspathContainer(base = "/org/eknet/publet/web/includes/publet")
    publet.mountManager.mount("/publet/".p, includes)
  }
}
