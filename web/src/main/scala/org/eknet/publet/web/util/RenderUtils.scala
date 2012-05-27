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

package org.eknet.publet.web.util

import org.eknet.publet.vfs.Content
import org.fusesource.scalate.TemplateSource
import org.eknet.publet.web.PubletWeb

/**
 * Utility methods for [[org.eknet.publet.engine.scala.ScalaScript]]s
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 21.05.12 08:46
 *
 */
object RenderUtils {

  def renderTemplate(uri: String, attributes: Map[String, Any]): Option[Content] = {
    val attr = PubletWeb.scalateEngine.attributes ++ attributes
    Some(PubletWeb.scalateEngine.processUri(uri, None, attr))
  }

  def renderTemplate(source: TemplateSource, attributes: Map[String, Any]): Option[Content] = {
    val attr = PubletWeb.scalateEngine.attributes ++ attributes
    Some(PubletWeb.scalateEngine.processSource(source, None, attr))
  }

  /**
   * Renders the message.
   *
   * @param title
   * @param message
   * @param level
   * @return
   */
  def renderMessage(title: String, message: String, level: String) = {
    val attr = Map(
      "title" -> title,
      "message" -> message,
      "level" -> level
    )
    renderTemplate(messagePage, attr)
  }

  /**A template rendering a simple message. Supply the following parameters:
   *
   * * message:String the message to display
   * * title: the page title
   * * level: String "error", "success" or "warning"
   *
   * You should use the `renderMessage()` method.
   *
   */
  val messagePage = "/publet/templates/_messagepage.page"

  /**A template that renders a login form.
   * There is only one optional attribute:
   *
   * * redirect: String - the uri to redirect after successful login
   */
  val loginPage = "/publet/templates/login.jade"
}
