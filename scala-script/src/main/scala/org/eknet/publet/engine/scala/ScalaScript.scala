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

import org.eknet.publet.vfs.{ContentType, Content}
import org.eknet.publet.com.twitter.json.Json
import java.io.InputStream
import grizzled.slf4j.Logging

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 10.04.12 21:31
 */

trait ScalaScript extends Logging {

  def serve(): Option[Content]

}

object ScalaScript {

  def makeJson(any: Any): Option[Content] = Some(Content(Json.build(any).toString, ContentType.json))

  def makeJs(str: String): Option[Content] = Some(Content(str, ContentType.javascript))

  def makePng(data: Array[Byte]): Option[Content] = Some(Content(data, ContentType.png))
  def makePng(data: InputStream): Option[Content] = Some(Content(data, ContentType.png))

  def makeJpg(data: Array[Byte]): Option[Content] = Some(Content(data, ContentType.jpg))
  def makeJpg(data: InputStream): Option[Content] = Some(Content(data, ContentType.jpg))
}