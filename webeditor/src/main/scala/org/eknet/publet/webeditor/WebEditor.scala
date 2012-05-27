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

package org.eknet.publet.webeditor

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.engine.scalate.ScalateEngine
import org.eknet.publet.web.{PubletWeb, PubletWebContext}
import org.eknet.publet.vfs.{Content, ContentType, ContentResource, Path}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.05.12 05:30
 */
class WebEditor(val name: Symbol, scalateEngine: ScalateEngine) extends PubletEngine {

  def process(path: Path, data: ContentResource, target: ContentType) = {
    if (!PubletWeb.publet.mountManager.resolveMount(path).map(_._2.isWriteable).getOrElse(false)) {
      val attr = Map(
      "message" -> "Content not writeable!"
      ) ++ scalateEngine.attributes
      Some(scalateEngine.processUri(EditorPaths.errorTemplate.asString, Some(data), attr))
    } else {
      val resourcePath = path.sibling(data.name.fullName).asString
      val ctx = PubletWebContext
      val uri = EditorPaths.editHtmlPage.asString+"?resource="+resourcePath
      ctx.redirect(ctx.urlOf(uri))
      Some(Content.empty(ContentType.html))
    }
  }

}
