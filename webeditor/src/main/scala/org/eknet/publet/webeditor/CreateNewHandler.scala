package org.eknet.publet.webeditor

import javax.servlet.http.HttpServletResponse
import org.eknet.publet.web.WebContext
import org.eknet.publet.web.filter.{PageWriter, NotFoundHandler}
import org.eknet.publet.vfs.{PathContentResource, ContentType, Content, Path}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 16:14
 */
class CreateNewHandler extends NotFoundHandler with PageWriter {
  def resourceNotFound(path: Path, resp: HttpServletResponse) {
    val out = resp.getOutputStream
    val targetType = path.name.targetType
    val publet = WebContext().webPublet.publet
    if (targetType.mime._1 == "text") {
      val res = new PathContentResource(path, Content.empty(ContentType.markdown))
      val c = publet.engineManager.getEngine('edit).get
        .process(Seq(res), ContentType.markdown);
      c.get.copyTo(out)
    } else {
      val uploadContent = UploadContent.uploadContent(path)
      val res = new PathContentResource(path, uploadContent)
      val c = publet.engineManager.resolveEngine(path).get
        .process(Seq(res), ContentType.html)
      writePage(Some(c.get), resp)
    }
  }
}
