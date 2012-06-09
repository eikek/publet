package org.eknet.publet.gitr.web.scripts

import java.util.List
import org.eclipse.jgit.diff.{RawText, EditList, DiffEntry, DiffFormatter}
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.patch.FileHeader.PatchType
import collection.mutable.ListBuffer
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import java.io.{ByteArrayOutputStream, OutputStream}
import org.eclipse.jgit.util.RawParseUtils

/**
 * Creates a html snippet for a diff. This is a modified version of the class
 * `GitBlitDiffFormatter.java` of the gitblit (http://gitblit.com) project.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.06.12 09:59
 */
class HtmlDiffFormatter(os: OutputStream) extends DiffFormatter(os) {

  private var left, right: Int = 0

  override def writeHunkHeader(aStartLine: Int, aEndLine: Int, bStartLine: Int, bEndLine: Int) {
    os.write("<tr><th>..</th><th>..</th><td class='hunk_header'>".getBytes)

    super.writeHunkHeader(aStartLine, aEndLine, bStartLine, bEndLine)
    os.write("</td></tr>\n".getBytes)
    left = aStartLine + 1
    right = bStartLine + 1
  }

  override def writeLine(prefix: Char, text: RawText, cur: Int) {
    os.write("<tr>".getBytes)
    prefix match {
      case '+' => {
        os.write(("<th></th><th>" + right + "</th>").getBytes)
        os.write("<td><div class=\"diff add2\">".getBytes)
        right = right +1
      }
      case '-' => {
        os.write(("<th>" + left + "</th><th></th>").getBytes)
        os.write("<td><div class=\"diff remove2\">".getBytes)
        left = left +1
      }
      case _ => {
        os.write(("<th>" + left + "</th><th>" + right + "</th>").getBytes)
        os.write("<td>".getBytes)
        left = left +1
        right = right +1
      }
    }

    os.write(prefix)
    val line = xml.Utility.escape(text.getString(cur))
    os.write(line.getBytes)
    if (prefix == '+' || prefix == '-') {
      os.write("</div>".getBytes)
    } else {
      os.write("</td>".getBytes)
    }
    os.write("</tr>\n".getBytes)
  }

  /**
   * Copy&Paste from gitblits `GitBlitDiffFormatter.java`.
   *
   * This method creates the final html output. It discards some
   * other output written into the stream from the base class
   * @return
   */
  def getHtml: String = {
    val bos = os.asInstanceOf[ByteArrayOutputStream]
    val html = RawParseUtils.decode(bos.toByteArray)
    val lines = html.split("\n")
    val sb = new StringBuilder()
    var inFile = false;
    val oldnull = "a/dev/null"
    for (line <- lines) {
      var theline = line
      if (line.startsWith("index")) {
        // skip index lines
      } else if (line.startsWith("new file")) {
        // skip new file lines
      } else if (line.startsWith("\\ No newline")) {
        // skip no new line
      } else if (line.startsWith("---") || line.startsWith("+++")) {
        // skip --- +++ lines
      } else if (line.startsWith("diff")) {
        if (line.indexOf(oldnull) > -1) {
          // a is null, use b
          theline = line.substring(("diff --git " + oldnull).length()).trim()
          // trim b/
          theline = theline.substring(2)
        } else {
          // use a
          theline = line.substring("diff --git a/".length()).trim()
          theline = theline.substring(0, theline.indexOf(" b/")).trim()
        }
        if (inFile) {
          sb.append("</tbody></table></div>\n")
          inFile = false
        }
//        sb.append("<div class='header'>").append(theline).append("</div>")
        sb.append("<div class=\"diff\">")
        sb.append("<table><tbody>")
        inFile = true
      } else {
        sb.append(theline)
      }
    }
    sb.append("</table></div>")
    sb.toString()
  }
}
