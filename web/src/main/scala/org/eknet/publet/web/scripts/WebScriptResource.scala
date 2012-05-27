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

package org.eknet.publet.web.scripts

import org.eknet.publet.web.util.{Request, Key}
import org.eknet.publet.engine.scala.{ScriptResource, ScalaScript}
import org.eknet.publet.vfs.ResourceName
import org.eknet.publet.web.PubletWebContext

/** A resource that executes the given script on access. The result
 * is cached as attribute inside the request.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 19:28
 */
class WebScriptResource(name: ResourceName, script: ScalaScript)
    extends ScriptResource(name, script) {

  override def evaluate = {
    val scriptResultKey = Key("publet.web.script.Result", {
      case Request => super.evaluate
    })

    PubletWebContext.attr(scriptResultKey).get
  }

}
