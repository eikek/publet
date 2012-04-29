package org.eknet.publet.webeditor

import javax.servlet.http.HttpServletResponse
import org.eknet.publet.web.WebContext
import org.eknet.publet.web.filter.{PageWriter, NotFoundHandler}
import org.eknet.publet.vfs._
import util.SimpleContentResource

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
      val res = new SimpleContentResource(ResourceName("edit"), Content.empty(ContentType.markdown))
      val c = publet.engineManager.getEngine('edit).get
        .process(path, Seq(res), ContentType.markdown);
      c.get.copyTo(out)
    } else {
      val uploadContent = UploadContent.uploadContent(path)
      val res = new SimpleContentResource(ResourceName("upload"), uploadContent)
      val c = publet.engineManager.resolveEngine(path).get
        .process(path, Seq(res), ContentType.html)
      writePage(Some(c.get), resp)
    }
  }
}
