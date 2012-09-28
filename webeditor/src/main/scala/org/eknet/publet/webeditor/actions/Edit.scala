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

package org.eknet.publet.webeditor.actions

import collection.mutable
import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.util.RenderUtils._
import xml.{Null, Text, Attribute}
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web.{PubletWeb, PubletWebContext}
import org.eknet.publet.vfs.{Resource, ContentType, ContentResource, Path}
import org.eknet.publet.webeditor.EditorPaths

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.05.12 20:45
 */
class Edit extends ScalaScript {
  import org.eknet.publet.webeditor.EditorPaths._

  def serve() = {
    val resourcePath = PubletWebContext.param("resource").map(Path(_))
    if (resourcePath.isEmpty) {
      renderErrorMessage("No resource specified.")
    } else {
      Security.checkWritePermission(resourcePath.get)
      val resource = PubletWeb.publet.rootContainer.lookup(resourcePath.get)
        .collect({ case c: ContentResource => c })
      resource map { r =>
        if (r.contentType.mime._1 == "text") handleTextContent(resourcePath.get, r)
        else handleBinaryContent(resourcePath.get, r)
      } getOrElse {
        val res = Resource.emptyContent(resourcePath.get.name, resourcePath.get.name.targetType)
        if (res.contentType.mime._1 == "text") {
          handleTextContent(resourcePath.get, res)
        } else {
          handleBinaryContent(resourcePath.get, res)
        }
      }
    }
  }



  def handleTextContent(resourcePath: Path, data: ContentResource) = {
    val attr = mutable.Map[String, Any]()
    attr += "contentAsString" -> data.contentAsString
    attr += "actionPath" -> EditorPaths.pushScript.asString
    attr += "lastMod" -> data.lastModification.map(_.toString).getOrElse("")
    attr += "resourcePath" -> resourcePath.sibling(data.name.fullName).asString
    attr += "extensionOptions" -> extensionOptions(resourcePath, Some(data))
    renderTemplate(editPageTemplate.asString, attr.toMap)
  }

  def handleBinaryContent(resourcePath: Path, data: ContentResource) = {
    val attr = mutable.Map[String, Any]()
    attr += "actionPath" -> EditorPaths.uploadScript.asString
    attr += "lastMod" -> data.lastModification.map(_.toString).getOrElse("")
    attr += "resourcePath" -> resourcePath.sibling(data.name.fullName).asString
    renderTemplate(uploadTemplate.asString, attr.toMap)
  }

  def renderErrorMessage(msg: String) = {
    renderTemplate(errorTemplate.asString, Map("message" -> msg))
  }

  def extensionOptions(path: Path, source: Option[ContentResource]) = {
    val list = ContentType.forMimeBase(PubletWebContext.applicationPath.name.targetType)
    val prefExt = source.map(_.name.ext).getOrElse("md")
    for (ct <- list) yield {
      <optgroup label={ct.typeName.name}>
        {for (ext <- ct.extensions.toList.sortWith(_ < _)) yield {
        val o = <option>
          {ext}
        </option>
        if (prefExt == ext) {
          o % Attribute("selected", Text("selected"), Null)
        } else {
          o
        }
      }}
      </optgroup>
    }
  }
}
