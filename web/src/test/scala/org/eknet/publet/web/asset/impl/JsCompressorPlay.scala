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

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import java.io._
import com.yahoo.platform.yui.compressor.{JavaScriptCompressor => JSCompressor}
import org.mozilla.javascript.{EvaluatorException, ErrorReporter}
import grizzled.slf4j.Logging
import io.Source

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.09.12 22:19
 */
class JsCompressorPlay extends FunSuite with ShouldMatchers with BeforeAndAfter {

  test ("yui compressor play") {
    val r = ResourceHelper.findResource("bootstrap/js/bootstrap.js").get
    val s = Source.fromURL(r, "UTF-8").getLines().mkString("\n")

    val out = new FileOutputStream(new File("/tmp/test.js"))
    compressJs(new StringReader(s), out)
    println()
  }

  def compressJs(in: Reader, out: OutputStream) {
    val compressor = new JSCompressor(in, Reporter)
    compressor.compress(new OutputStreamWriter(out), -1, true, false, false, false)
    out.close()
    in.close()
  }

  object Reporter extends ErrorReporter with Logging {
    def warning(message: String, sourceName: String, line: Int, lineSource: String, lineOffset: Int) {
      warn(message)
    }

    def error(message: String, sourceName: String, line: Int, lineSource: String, lineOffset: Int) {
      error(message)
    }

    def runtimeError(message: String, sourceName: String, line: Int, lineSource: String, lineOffset: Int) = {
      error(message)
      new EvaluatorException(message, sourceName, line, lineSource, lineOffset)
    }

  }
}
