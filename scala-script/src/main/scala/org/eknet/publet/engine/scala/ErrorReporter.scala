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

import tools.nsc.reporters.AbstractReporter
import tools.nsc.util.Position
import tools.nsc.Settings
import collection.mutable

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.04.12 14:16
 */
class ErrorReporter(val settings: Settings, lineOff: Int = 0) extends AbstractReporter {

  val messages = new mutable.ListBuffer[List[String]]

  def display(pos: Position, message: String, severity: Severity) {
    severity.count += 1
    val severityName = severity match {
      case ERROR => "error: "
      case WARNING => "warning: "
      case _ => ""
    }
    messages += (severityName + "line " + (pos.line - lineOff) + ": " + message) ::
      (if (pos.isDefined) {
        pos.inUltimateSource(pos.source).lineContent.stripLineEnd ::
          (" " * (pos.column - 1) + "^") ::
          Nil
      } else {
        Nil
      })
  }

  def displayPrompt() {
    // no.
  }

  override def reset() {
    super.reset()
    messages.clear()
  }

  def htmlEscapeMessages = {
    messages.map(_.map(s => s.replace("<", "&lt;").replace(">", "&gt;")))
  }
}
