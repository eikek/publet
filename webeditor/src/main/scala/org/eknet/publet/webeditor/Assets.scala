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

import org.eknet.publet.vfs.util.SimpleContentResource
import org.eknet.publet.vfs.ResourceName._
import org.eknet.publet.web.asset.{AssetCollection, Group}
import scala.Some
import scala.io.Source
import org.eknet.publet.vfs.{ContentType, Content}
import org.eknet.publet.web.template.DefaultLayout
import org.eknet.publet.web.Config
import org.eknet.publet.web.util.PubletWeb

/**
 * Asset definitions for the webeditor. JQuery is not included as requirement
 * because those assets are loaded additionally to the default template. It
 * is expected that jquery is already loaded.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.09.12 17:13
 */
object Assets extends AssetCollection {

  override def classPathBase = "/org/eknet/publet/webeditor/includes"

  val codemirror = Group("codemirror")
    .add(resource("codemirror/lib/codemirror.css"))
    .add(resource("codemirror/lib/codemirror.js"))
    .add(resource("codemirror/lib/util/active-line.js"))
    .add(resource("codemirror/lib/util/closetag.js"))
    .add(resource("codemirror/lib/util/dialog.js"))
    .add(resource("codemirror/lib/util/dialog.css"))
    .add(resource("codemirror/lib/util/foldcode.js"))
    .add(resource("codemirror/lib/util/formatting.js"))
    .add(resource("codemirror/lib/util/html-hint.js"))
    .add(resource("codemirror/lib/util/javascript-hint.js"))
    .add(resource("codemirror/lib/util/mark-selection.js"))
    .add(resource("codemirror/lib/util/match-highlighter.js"))
    .add(resource("codemirror/lib/util/matchbrackets.js"))
    .add(resource("codemirror/lib/util/search.js"))
    .add(resource("codemirror/lib/util/searchcursor.js"))
    .add(resource("codemirror/lib/util/simple-hint.js"))
    .add(resource("codemirror/lib/util/simple-hint.css"))
    .add(resource("codemirror/lib/util/xml-hint.js"))
    .add(resource("codemirror/mode/clike/clike.js"))
    .add(resource("codemirror/mode/css/css.js"))
    .add(resource("codemirror/mode/javascript/javascript.js"))
    .add(resource("codemirror/mode/markdown/markdown.js"))
    .add(resource("codemirror/mode/xml/xml.js"))
    .add(resource("codemirror/mode/properties/properties.js"))
    .add(resource("codemirror/mode/htmlmixed/htmlmixed.js"))

  val codemirrorJquery = Group("codemirror.jquery")
    .add(resource("js/codemirror.jquery.js"))
    .require(codemirror.name, DefaultLayout.Assets.jquery.name)

  val jqueryUiWidget = Group("jquery.ui.widget")
    .add(resource("js/vendor/jquery.ui.widget.js"))
    .require(DefaultLayout.Assets.jquery.name)

  val blueimpTmpl = Group("blueimp.tmpl")
    .add(resource("js/blueimp/tmpl.min.js"))

  val blueimpCanvasToBlob = Group("blueimp.canvastoblob")
    .add(resource("js/blueimp/canvas-to-blob.min.js"))

  val blueimpLoadImage = Group("blueimp.loadimage")
    .add(resource("js/blueimp/load-image.min.js"))
    .require(blueimpCanvasToBlob.name)

  val jqueryIframeTransport = Group("jquery.iframe-transport")
    .add(resource("js/jquery.iframe-transport.js"))
    .require(DefaultLayout.Assets.jquery.name)

  val blueimpFileUpload = Group("blueimp.fileupload")
    .add(resource("img/loading.gif"))
    .add(resource("img/progressbar.gif"))
    .add(resource("img/publet_nopreview.png"))
    .add(resource("css/jquery.fileupload-ui.css"))
    .add(resource("js/jquery.fileupload.js"))
    .add(resource("js/jquery.fileupload-fp.js"))
    .add(resource("js/jquery.fileupload-ui.js"))
    .add(resource("js/locale.js"))
    .require(jqueryIframeTransport.name, jqueryUiWidget.name,
              blueimpTmpl.name, blueimpLoadImage.name, DefaultLayout.Assets.jquery.name)


  val publetFileBrowser = Group("publet.webeditor.filebrowser")
    .add(resource("js/publet-browser.js").noCompress) //todo: syntax errors detected while compressing
    .add(resource("css/publet-browser.css"))
    .require(DefaultLayout.Assets.jquery.name)

  val editpageBrowser = Group("publet.webeditor.browserloader")
    .add(new SimpleContentResource("browser.js".rn, Content(generateBrowserLoadJs, ContentType.javascript)))
    .require(publetFileBrowser.name)

  val editPage = Group("publet.webeditor.editpage")
    .add(resource("css/edit-page.css"))
    .add(resource("js/edit-page.js"))
    .require(codemirrorJquery.name, editpageBrowser.name)

  val uploadPage = Group("publet.webeditor.uploadpage")
    .use(blueimpFileUpload.name, editpageBrowser.name)


  private def generateBrowserLoadJs: String = {
    val pathRegex = ("\"("+ EditorPaths.editorPath.asString +"[^\"]*\")").r

    def replaceUrlInJsSource(urlfun: String => String): String = {
      val file = Thread.currentThread().getContextClassLoader.getResource("org/eknet/publet/webeditor/includes/js/browser_templ.js")
      Source.fromURL(file, "UTF-8").getLines().map(line =>
        pathRegex.findFirstMatchIn(line) match {
          case Some(m) => m.before +"\""+ urlfun(m.group(1)) + m.after
          case None => line
        }
      ).mkString("\n")
    }
    replaceUrlInJsSource(s => {
      Config("publet.urlBase").getOrElse(PubletWeb.contextPath) + s
    })
  }

}
