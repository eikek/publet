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

package org.eknet.publet.engine.scala

import java.io.OutputStream
import org.eknet.publet.vfs.{ContentType, Content, ResourceName, ContentResource}

/**A resource that executes the given script on each access.
 *
 * Note, that the script is executed by almost all methods (for
 * example to get the content type). You need to implement the
 * desired caching strategy. In web environments you'd cache
 * the result per request.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 24.04.12 23:09
 */
abstract class ScriptResource(val name: ResourceName, val script: ScalaScript) extends ContentResource {

  protected def evaluate = script.serve().getOrElse(Content.empty(ContentType.html))

  override def lastModification = evaluate.lastModification

  def inputStream = evaluate.inputStream

  def contentType = evaluate.contentType

  val exists = true

  override def copyTo(out: OutputStream, close: Boolean = true) {
    evaluate.copyTo(out, close)
  }

  override def contentAsString(charset: String = "UTF-8") = evaluate.contentAsString(charset)

  override def toString = "Script:" + name
}

