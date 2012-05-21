package org.eknet.publet.webeditor.actions

import collection.mutable
import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.util.RenderUtils._
import org.eknet.publet.webeditor.EditorWebExtension._
import org.eknet.publet.web.{PubletWeb, PubletWebContext}
import org.eknet.publet.webeditor.EditorWebExtension
import org.eknet.publet.vfs.{ContentType, ContentResource, Path}
import xml.{Null, Text, Attribute}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.05.12 20:45
 */
class Edit extends ScalaScript {
  def serve() = {
    val resourcePath = PubletWebContext.param("resource").map(Path(_))
    val resource = resourcePath
      .flatMap(PubletWeb.publet.rootContainer.lookup)
      .collect({
      case c: ContentResource => c
    })
    resource map { r =>
      if (r.contentType.mime._1 == "text") handleTextContent(resourcePath.get, r)
      else handleBinaryContent(resourcePath.get, r)
    } getOrElse {
      renderErrorMessage("Resource not found.")
    }
  }

  def handleTextContent(resourcePath: Path, data: ContentResource) = {
    val attr = mutable.Map[String, Any]()
    attr += "contentAsString" -> data.contentAsString
    attr += "actionPath" -> pushPath(resourcePath).asString
    attr += "lastMod" -> data.lastModification.map(_.toString).getOrElse("")
    attr += "resourcePath" -> resourcePath.sibling(data.name.fullName).asString
    attr += "extensionOptions" -> extensionOptions(resourcePath, Some(data))
    renderTemplate(editPageTemplate, attr.toMap)
  }

  def handleBinaryContent(resourcePath: Path, data: ContentResource) = {
    renderErrorMessage("Not implemented yet :(")
  }

  def renderErrorMessage(msg: String) = {
    renderTemplate(errorTemplate, Map("message" -> msg))
  }

  def pushPath(path: Path) = EditorWebExtension.scriptPath / "push.json"

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
