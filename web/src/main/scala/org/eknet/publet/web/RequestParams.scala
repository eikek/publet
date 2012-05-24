package org.eknet.publet.web

import scala.collection.JavaConversions._

import javax.servlet.http.HttpServletRequest
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.FileItem
import util.{Request, Key}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 21:04
 */
trait RequestParams {

  protected def req: HttpServletRequest

  def params = req.getParameterMap.map(t => t._1.asInstanceOf[String] -> t._2.asInstanceOf[Array[String]].toList)

  def param(name: String) = {
    Option(req.getParameter(name)).orElse {
      multipartFormFields.find(_.getFieldName==name).map(_.getString)
    }
  }

  def uploads = multipartFields.filter(!_.isFormField)

  private val multipartFieldsKey = Key("reqMultipartFields", {
    case Request => {
      val rct = req.getContentType
      if (rct != null && rct.startsWith("multipart")) {
        val items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req)
        items.collect({case p:FileItem => p}).toList
      } else {
        List[FileItem]()
      }
    }
  })

  def multipartFields = PubletWebContext.attr(multipartFieldsKey).get

  private def multipartFormFields = multipartFields.filter(_.isFormField)
}
