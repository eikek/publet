package org.eknet.publet.webeditor

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.engine.scalate.ScalateEngine
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.vfs.{ContentType, ContentResource, Path}
import xml.{Null, Text, Attribute}
import org.eknet.publet.web.{PubletWeb, PubletWebContext, GitAction}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.05.12 05:30
 */
class WebEditor(val name: Symbol, scalateEngine: ScalateEngine) extends PubletEngine {

  val editPageTemplate = "/publet/webeditor/templates/editpage.page"
  val uploadTemplate = "/publet/webeditor/templates/uploadpage.page"
  val errorTemplate = "/publet/webeditor/templates/errorpage.page"
  def pushPath(path: Path) = EditorWebExtension.scriptPath / "push.json"

  def process(path: Path, data: ContentResource, target: ContentType) = {
    Security.checkGitAction(GitAction.push)
    if (!PubletWeb.publet.mountManager.resolveMount(path).map(_._2.isWriteable).getOrElse(false)) {
      val attr = Map(
      "message" -> "Content not writeable!"
      ) ++ scalateEngine.attributes
      Some(scalateEngine.processUri(errorTemplate, attr))
    } else {
      if (data.contentType.mime._1 == "text") {
        val attr = Map(
          "contentAsString" -> data.contentAsString,
          "actionPath" -> pushPath(path).asString,
          "lastMod" -> data.lastModification.map(_.toString).getOrElse(""),
          "resourcePath" -> path.sibling(data.name.fullName).asString,
          "extensionOptions" -> extensionOptions(path, Some(data))
        ) ++ scalateEngine.attributes
        Some(scalateEngine.processUri(editPageTemplate, attr))
      } else {
        val attr = Map(
          "actionPath" -> pushPath(path).asString,
          "lastMod" -> data.lastModification.map(_.toString).getOrElse(""),
          "resourcePath" -> path.sibling(data.name.fullName).asString
        ) ++ scalateEngine.attributes
        Some(scalateEngine.processUri(uploadTemplate, attr))
      }
    }
  }

  def extensionOptions(path: Path, source: Option[ContentResource]) = {
    val list = ContentType.forMimeBase(PubletWebContext.applicationPath.name.targetType)
    val prefExt = source.map(_.name.ext).getOrElse("md")
    for (ct <- list) yield {
      <optgroup label={ct.typeName.name}>
        {
        for (ext<-ct.extensions.toList.sortWith(_ < _)) yield {
          val o = <option>{ext}</option>
          if (prefExt == ext) {
            o % Attribute("selected", Text("selected"), Null)
          } else {
            o
          }
        }
        }
      </optgroup>
    }
  }

}
