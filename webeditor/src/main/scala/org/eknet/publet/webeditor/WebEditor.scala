package org.eknet.publet.webeditor

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.engine.scalate.ScalateEngine
import org.eknet.publet.web.{PubletWeb, PubletWebContext}
import org.eknet.publet.vfs.{Content, ContentType, ContentResource, Path}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.05.12 05:30
 */
class WebEditor(val name: Symbol, scalateEngine: ScalateEngine) extends PubletEngine {

  def process(path: Path, data: ContentResource, target: ContentType) = {
    if (!PubletWeb.publet.mountManager.resolveMount(path).map(_._2.isWriteable).getOrElse(false)) {
      val attr = Map(
      "message" -> "Content not writeable!"
      ) ++ scalateEngine.attributes
      Some(scalateEngine.processUri(EditorPaths.errorTemplate.asString, attr))
    } else {
      val resourcePath = path.sibling(data.name.fullName).asString
      PubletWebContext.redirect(PubletWebContext.contextPath + EditorPaths.editHtmlPage.asString+"?resource="+resourcePath)
      Some(Content.empty(ContentType.html))
    }
  }

}
