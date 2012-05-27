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
import org.eknet.publet.vfs.{Path, ContentType, Content}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 10:11
 */
trait ConverterEngine extends PubletEngine {

  type Converter = (Path, Content) => (Content)

  def addConverter(mapping: (ContentType, ContentType), c: ConverterEngine#Converter)

}

object ConverterEngine {

  def compose(c1: ConverterEngine#Converter, c2: ConverterEngine#Converter) = (p:Path, c:Content) => c1(p, c2(p,c))
}