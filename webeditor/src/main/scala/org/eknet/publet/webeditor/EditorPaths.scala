package org.eknet.publet.webeditor

import org.eknet.publet.vfs.Path

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 24.05.12 11:33
 */
object EditorPaths {


  val editorPath = Path("/publet/webeditor/")
  val scriptPath = editorPath / "scripts"
  val templatePath = editorPath / "templates"

  val uploadScript = scriptPath / "/upload.json"
  val pushScript = scriptPath / "/push.json"
  val editHtmlPage = scriptPath / "edit.html"
  val thumbNailer = scriptPath / "thumb.png"


  val editPageTemplate = templatePath / "editpage.page"
  val uploadTemplate = templatePath / "uploadpage.page"
  val errorTemplate = templatePath / "errorpage.page"
}
