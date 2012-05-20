package org.eknet.publet.webeditor

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.engine.scalate.ScalateEngine
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.vfs.{Writeable, ContentType, ContentResource, Path}
import org.eknet.publet.web.{PubletWebContext, GitAction}
import xml.{Null, Text, Attribute}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.05.12 05:30
 */
class WebEditor(val name: Symbol, scalateEngine: ScalateEngine) extends PubletEngine {

  val editPageTemplate = "/publet/webeditor/templates/editpage.page"
  def pushPath(path: Path) = EditorWebExtension.scriptPath / "push.json"

  def process(path: Path, data: Seq[ContentResource], target: ContentType) = {
    Security.checkGitAction(GitAction.push)
    if (!data.head.exists || data.head.isInstanceOf[Writeable]) {
      if (data.head.contentType.mime._1 == "text") {
        val attr = Map(
          "contentAsString" -> data.head.contentAsString,
          "actionPath" -> pushPath(path).asString,
          "lastMod" -> data.head.lastModification.map(_.toString).getOrElse(""),
          "resourcePath" -> path.sibling(data.head.name.fullName).asString,
          "extensionOptions" -> extensionOptions(path, Some(data.head))
        ) ++ scalateEngine.attributes
        Some(scalateEngine.processUri(editPageTemplate, attr))
      } else {
        sys.error("not implemented")
      }
    } else {
      sys.error("not implemented")
      //      val c = new CompositeContentResource(data.head, jsFunction(message("Resource is not writeable", Some("error"))).get)
      //      del.process(path, Seq(c), ContentType.html)
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
