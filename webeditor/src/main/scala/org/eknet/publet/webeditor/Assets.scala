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

import org.eknet.publet.vfs.util.{SimpleContentResource, UrlResource}
import org.eknet.publet.vfs.Path._
import org.eknet.publet.vfs.ResourceName._
import org.eknet.publet.web.{Config, PubletWeb}
import org.eknet.publet.web.asset.Group
import scala.Some
import io.Source
import org.eknet.publet.vfs.{ContentType, Content}

/**
 * Asset definitions for the webeditor. JQuery is not included as requirement
 * because those assets are loaded additionally to the default template. It
 * is expected that jquery is already loaded.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.09.12 17:13
 */
object Assets {

  lazy val jqueryUiWidget = Group("jquery.ui.widget")
    .add(resource("js/vendor/jquery.ui.widget.js"))

  lazy val blueimpTmpl = Group("blueimp.tmpl")
    .add(resource("js/blueimp/tmpl.min.js"))

  lazy val blueimpCanvasToBlob = Group("blueimp.canvastoblob")
    .add(resource("js/blueimp/canvas-to-blob.min.js"))

  lazy val blueimpLoadImage = Group("blueimp.loadimage")
    .add(resource("js/blueimp/load-image.min.js"))
    .require(blueimpCanvasToBlob.name)

  lazy val jqueryIframeTransport = Group("jquery.iframe-transport")
    .add(resource("js/jquery.iframe-transport.js"))

  lazy val blueimpFileUpload = Group("blueimp.fileupload")
    .add(new SimpleContentResource("fileupload-ui.css".rn, Content(replaceRelativeImgUrls, ContentType.css)))
    .add(resource("js/jquery.fileupload.js"))
    .add(resource("js/jquery.fileupload-fp.js"))
    .add(resource("js/jquery.fileupload-ui.js"))
    .add(resource("js/locale.js"))
    .add(resource("js/main.js"))
    .require(jqueryIframeTransport.name, jqueryUiWidget.name,
              blueimpTmpl.name, blueimpLoadImage.name)


  lazy val publetFileBrowser = Group("publet.webeditor.filebrowser")
    .add(resource("js/publet-browser.js"))
    .add(resource("css/browser.css"))

  lazy val editpageBrowser = Group("publet.webeditor.editpage.browser")
    .add(new SimpleContentResource("browser.js".rn, Content(generateBrowserLoadJs, ContentType.javascript)))
    .require(publetFileBrowser.name)

  lazy val editPage = Group("publet.webeditor.editpage")
    .use(editpageBrowser.name)

  lazy val uploadPage = Group("publet.webeditor.uploadpage")
    .use(blueimpFileUpload.name, editpageBrowser.name)

  private def findResource(name: String) = Option(getClass.getResource(
    ("/org/eknet/publet/webeditor/includes".p / name).asString))

  private def resource(name: String) = new UrlResource(findResource(name)
    .getOrElse(sys.error("Resource '"+ name + "' not found")))

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
      Config("publet.urlBase").getOrElse(PubletWeb.servletContext.getContextPath) + s
    })
  }

  private def replaceRelativeImgUrls: String = {
    val file = findResource("css/jquery.fileupload-ui.css").get
    Source.fromURL(file, "UTF-8").getLines().map(line => {
      if (line.contains("../img/"))
        line.replace("../img/", "../webeditor/img/")
      else
        line
    }).mkString("\n")
  }
}
