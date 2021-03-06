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

import org.fusesource.scalate.TemplateSource
import org.eknet.publet.vfs.{Content, Path}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 00:17
 */
class TemplateResource(val uri: String, val content: Content) extends TemplateSource {
  private val time = System.currentTimeMillis()

  def this(path: Path, content: Content) = this(path.asString, content)

  def inputStream = content.inputStream
  def lastModified = content.lastModification.getOrElse(time)

  override def toString = content.toString
}
