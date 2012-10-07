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

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.Publet
import org.fusesource.scalate.{TemplateSource, TemplateEngine}
import org.eknet.publet.vfs.{Content, ContentResource}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.10.12 01:22
 */
trait ScalateEngine extends PubletEngine {

  def attributes: Map[String, Any]
  def attributes_=(m: Map[String, Any])

  def engine: TemplateEngine
  def setDefaultLayoutUri(uri: String)
  def disableLayout()

  def processUri(uri: String, data: Option[ContentResource], attributes: Map[String, Any] = Map()): Content
  def processSource(source: TemplateSource, data: Option[ContentResource], attributes: Map[String, Any] = Map()): Content
}

object ScalateEngine {

  def apply(name: Symbol, publet: Publet): ScalateEngine = {
    val engine = new TemplateEngine
    VfsResourceLoader.install(engine, publet)
    new ScalateEngineImpl(name, engine)
  }
}