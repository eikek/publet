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

package org.eknet.publet.web.asset

import impl.{JavascriptCompressor, CssCompressor}

/**
 * An enumeration for resource types. The values contain
 * the file extension and a processor used for compressing
 * or writing.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.09.12 21:39
 */
object Kind extends Enumeration {

  val css = KindVal("css", new CssCompressor)
  val js = KindVal("js", new JavascriptCompressor)

  case class KindVal(ext: String, processor: AssetProcessor) extends Val(ext)
}
