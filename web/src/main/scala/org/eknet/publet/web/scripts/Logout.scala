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

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.shiro.Security
import ScalaScript._
import org.eknet.publet.web.PubletWebContext

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.05.12 20:49
 */
object Logout extends ScalaScript {

  def serve() = {
    if (Security.isAuthenticated) {
      Security.subject.logout()
    }
    PubletWebContext.param("redirect") match {
      case Some(uri) => PubletWebContext.redirect(uri)
      case _ =>
    }
    makeJson(Map("success"->true, "message"->"Logged out."))
  }
}
