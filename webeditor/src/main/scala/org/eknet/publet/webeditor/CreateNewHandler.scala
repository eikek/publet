package org.eknet.publet.webeditor

import javax.servlet.http.HttpServletResponse
import org.eknet.publet.web.filter.{PageWriter, NotFoundHandler}
import org.eknet.publet.vfs._
import grizzled.slf4j.Logging
import org.eknet.publet.web.{ErrorResponse, PubletWebContext, PubletWeb}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 16:14
 */
class CreateNewHandler extends NotFoundHandler with PageWriter with Logging {
  def resourceNotFound(path: Path, resp: HttpServletResponse) {
    val targetType = path.name.targetType
    val publet = PubletWeb.publet
    publet.mountManager.resolveMount(path) map { x=>

      val appPath = PubletWebContext.applicationPath
      val c = if (targetType.mime._1 == "text") {
        val res = Resource.emptyContent(appPath.name, ContentType.markdown)
        publet.engineManager.getEngine('edit).get
          .process(path, res, ContentType.markdown)
      } else {
        val res = Resource.emptyContent(appPath.name, targetType)
        publet.engineManager.getEngine('edit).get
          .process(path, res, targetType)
      }
      writePage(Some(c.get), resp)

    } getOrElse {
      error("Path not mounted: "+ path.asString)
      ErrorResponse.notFound.send(resp)
    }
  }
}
