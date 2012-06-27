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

package org.eknet.publet.webeditor

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.eknet.publet.web.filter.{PageWriter, NotFoundHandler}
import org.eknet.publet.vfs._
import grizzled.slf4j.Logging
import org.eknet.publet.web.{ErrorResponse, PubletWebContext, PubletWeb}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 16:14
 */
class CreateNewHandler extends NotFoundHandler with PageWriter with Logging {

  def resourceNotFound(path: Path, req: HttpServletRequest, resp: HttpServletResponse) {
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
      writePage(Some(c.get), req, resp)

    } getOrElse {
      error("Path not mounted: "+ path.asString)
      ErrorResponse.notFound.send(req, resp)
    }
  }

}
