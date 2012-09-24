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

package org.eknet.publet.engine.convert

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.vfs.{Path, ContentResource, ContentType, Content}
import grizzled.slf4j.Logging

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 12:46
 */
class DefaultConverterEngine(val name: Symbol) extends PubletEngine with ConverterEngine with ConverterRegistry with Logging {

  def this() = this('convert)

  def process(path: Path, data: ContentResource, target: ContentType): Option[Content] = {
    //if target type is available return it, otherwise try to process
    if (data.contentType == target) {
      Some(data)
    } else {
      converterFor(data.contentType, target) match {
        case None => {
          error("no converter found: "+ data.contentType+" -> "+ target)
          None
        }
        case Some(c) => Option(c(path, data))
      }
    }
  }

}
