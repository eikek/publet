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
