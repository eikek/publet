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

package org.eknet.publet.webeditor.actions

import org.eknet.publet.engine.scala.ScalaScript
import ScalaScript._
import scala.io.Source
import org.eknet.publet.webeditor.EditorPaths
import org.eknet.publet.web.PubletWebContext

/** Replaces all absolute urls by prefixing the context path.
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 22:08
 */
object BrowserJs extends ScalaScript {

  val pathRegex = ("\"("+ EditorPaths.editorPath.asString +"[^\"]*\")").r

  def serve() = {
    val file = Thread.currentThread().getContextClassLoader.getResource("org/eknet/publet/webeditor/includes/js/browser_templ.js")
    makeJs(Source.fromURL(file, "UTF-8").getLines().map(line =>
      pathRegex.findFirstMatchIn(line) match {
        case Some(m) => m.before +"\""+ PubletWebContext.urlOf(m.group(1)) + m.after
        case None => line
      }
    ).mkString("\n"))
  }
}
