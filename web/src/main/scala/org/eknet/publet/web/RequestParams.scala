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

  def params = req.getParameterMap.map(t => t._1-> t._2.toList)

  def param(name: String): Option[String] = {
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
