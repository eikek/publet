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

package org.eknet.publet.ext

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.util.{PubletWeb, PubletWebContext}
import org.eknet.publet.web.Config

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 15.04.12 23:16
 */
class MailContact extends ScalaScript with MailSupport {
  import org.eknet.publet.web.util.RenderUtils.makeJson

  def serve() = {
    val ctx = PubletWebContext
    import ctx._

    if (Config("smtp.host").isEmpty || Config("defaultReceiver").isEmpty) {
      jsonError("Mailer not configured! Sorry, the contact form is not working.")
    } else {
      val appName = PubletWeb.publetSettings("applicationName").map("["+ _ +"] ").getOrElse("")
      val from = param("from").collect({ case f if (!f.isEmpty) => f})
      val msg = param("message").collect({ case f if (!f.isEmpty) => f})
      val invisible = param("text").exists(!_.isEmpty)
      if (from.isDefined && msg.isDefined && !invisible) {
        newMail(from.get)
          .to(Config("defaultReceiver").get)
          .subject(appName+ "Contact Form")
          .text(msg.get)
          .send()
        jsonSuccess("Mail successfully sent.")
      } else {
        jsonError("Mailer not configured! Sorry, the contact form is not working.")
      }
    }
  }

  def jsonError(msg: String) = makeJson(Map("success"-> false, "message"->msg))
  def jsonSuccess(msg: String) = makeJson(Map("success"-> true, "message"->msg))
}
