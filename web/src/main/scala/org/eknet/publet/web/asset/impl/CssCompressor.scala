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

package org.eknet.publet.web.asset.impl

import org.eknet.publet.vfs.Writeable
import org.eknet.publet.web.asset.AssetProcessor
import java.io._
import com.yahoo.platform.yui.compressor.{CssCompressor => YuiCompressor}
import org.eknet.publet.web.asset.AssetResource

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.09.12 01:28
 */
class CssCompressor extends AssetProcessor {

  def createResource(list: List[AssetResource], target: Writeable) {
    val ins = list.map(_.inputStream)
    val input = ConcatInputStream(ins)
    val out = target.outputStream
    compressCss(input, out)
    out.flush()
    out.close()
  }

  def compressCss(in: InputStream, out: OutputStream) {
    val comp = new YuiCompressor(new InputStreamReader(in, "UTF-8"))
    val writer = new BufferedWriter(new OutputStreamWriter(out))
    comp.compress(writer, -1)
    writer.flush()
  }
}
