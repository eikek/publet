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

import org.eknet.publet.vfs.{ContentResource, Writeable, Content}
import org.eknet.publet.web.asset.AssetProcessor
import org.mozilla.javascript.{EvaluatorException, ErrorReporter}
import java.io._
import com.yahoo.platform.yui.compressor.{JavaScriptCompressor => JSCompressor}
import grizzled.slf4j.Logging
import com.google.javascript.jscomp._
import java.util.logging.Level

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.09.12 01:09
 */
class JavascriptCompressor extends AssetProcessor with Logging {

  def createResource(list: List[ContentResource], target: Writeable) {
    val out = target.outputStream
    val ins = list.flatMap(c => {
      List(c.inputStream, new ByteArrayInputStream("\n\n".getBytes("UTF-8")))
    })
//    compressGoogle("module", ConcatInputStream(ins), out)
    Content.copy(ConcatInputStream(ins), out, true, true)
    out.close()
  }

  def compressJs(in: InputStream, out: OutputStream) {
    val compressor = new JSCompressor(new InputStreamReader(in, "UTF-8"), Reporter)
    compressor.compress(new OutputStreamWriter(out), -1, true, false, false, false)
  }

  def compressGoogle(name: String, ins: InputStream, out: OutputStream) {
    Compiler.setLoggingLevel(Level.WARNING)
    val compiler = new Compiler()
    val options = new CompilerOptions
    CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options)

    val ext = JSSourceFile.fromCode("externs.js", "")
    val in = JSSourceFile.fromInputStream(name, ins)
    compiler.setErrorManager(new BasicErrorManager {
      final val formatter: MessageFormatter = ErrorFormat.SOURCELESS.toFormatter(null, false)
      def printSummary() {
        if (getErrorCount + getWarningCount > 0) {
          error(getErrorCount+ " error(s), "+getWarningCount+" warning(s)")
        }
      }

      def println(level: CheckLevel, err: JSError) {
        if (level == CheckLevel.ERROR) {
          error(err.format(level, formatter))
        }
        if (level == CheckLevel.WARNING) {
          warn(err.format(level, formatter))
        }
      }
    })
    compiler.compile(ext, in, options)
    if (compiler.hasErrors) {
      error("Error compiling file: "+ name+"! Including source file.")
      Content.copy(ins, out, false, false)
    } else {
      val w = new BufferedWriter(new OutputStreamWriter(out))
      w.write(compiler.toSource)
    }
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
