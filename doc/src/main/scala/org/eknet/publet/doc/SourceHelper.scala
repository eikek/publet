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

package org.eknet.publet.doc

import io.Source

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.10.12 13:00
 */
object SourceHelper {

  def printSource(name: String) = {
    Source.fromURL(getClass.getResource("/org/eknet/publet/doc/resources/_sources/"+ name), "UTF-8")
      .getLines()
      .withFilter(!_.trim.isEmpty)
      .drop(15) //remove copyright notice
      .mkString("\n")
  }
}
