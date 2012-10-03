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

import java.io.InputStream
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.eknet.publet.vfs.{ContentType, Content}
import com.google.common.net.HttpHeaders

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.06.12 11:08
 */
class StreamResponse(override val inputStream: InputStream, mime: String, override val length: Option[Long], name: String) extends CustomContent {

  override def contentType = {
    val sym = Symbol(mime.replace('/', 'S'))
    val mimeRegex = "([^/]+)/(.*)".r
    val mimeTuple = mime match {
      case mimeRegex(b, s) => (b, s)
      case _ => ("application", "octed-stream")
    }
    ContentType(sym, Set(), mimeTuple)
  }

  def send(req: HttpServletRequest, res: HttpServletResponse) {
    res.setContentType(mime)
    length.foreach(l => res.setContentLength(l.toInt))
    res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+name)

    val os = res.getOutputStream
    Content.copy(inputStream, os, true, false)
  }
}
