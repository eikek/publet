package org.eknet.publet.engine.scala

import tools.nsc.reporters.AbstractReporter
import tools.nsc.util.Position
import tools.nsc.Settings
import collection.mutable
import org.eknet.publet.engine.convert.CodeHtmlConverter
import org.eknet.publet.vfs.{ContentType, Content, Path}

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
    messages.map(_.map(s => CodeHtmlConverter.replaceChars(s)))
  }
}
