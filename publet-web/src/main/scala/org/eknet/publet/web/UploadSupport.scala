package org.eknet.publet.web

import javax.servlet.http.HttpServletRequest
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.FileItem
import collection.mutable.ListBuffer
import scala.collection.JavaConversions._

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 19:56
 */
class UploadSupport(req: HttpServletRequest) {

  def uploads: List[FileItem] = {
    val buffer = new ListBuffer[FileItem]
    if (req.getContentType.startsWith("multipart")) {
      try {
        val items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
        items.foreach(_ match {
          case i: FileItem => if (!i.isFormField) buffer+=i
        })
      }
    }
    buffer.toList
  }
}
