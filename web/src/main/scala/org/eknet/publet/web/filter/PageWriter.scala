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

package org.eknet.publet.web.filter

import javax.servlet.http.HttpServletResponse
import org.eknet.publet.vfs._
import scala.Some
import grizzled.slf4j.Logging
import org.eknet.publet.Publet
import org.eknet.publet.web.util.RenderUtils
import org.eknet.publet.web._

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 15:12
 *
 */
trait PageWriter extends Logging {

  /**
   * Gets the (processed) custom error page if available.
   *
   * @param code
   * @return
   */
  def getCustomErrorPage(code: Int): Option[Content] = {
    val publet = PubletWeb.publet
    val pageName = code.toString + ".html"
    publet.process(Path(Config.mainMount +"/"+ Publet.allIncludes + pageName).toAbsolute)
  }

  /**
   * Writes the http error by either delegating to a custom error page
   * or writing the error code into the response.
   *
   * @param code
   * @param resp
   */
  def writeError(code: Int, resp: HttpServletResponse) {
    writePage(Some(getCustomErrorPage(code).getOrElse(ErrorResponse(code))), resp)
  }

  /**
   * In development mode, writes the exception to the page. Otherwise
   * delegates to `writeError` for rendering an error page.
   * @param ex
   * @param resp
   */
  def writeError(ex: Throwable, resp: HttpServletResponse) {
    if (Config("publet.mode").getOrElse("development") == "development") {
      //print the exception in development mode
      try {
        writePage(RenderUtils.renderException(ex), resp)
      }
      catch {
        case e:Throwable => {
          error("Error while writing application exception!", e)
          writeError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resp)
        }
      }
    } else {
      writeError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resp)
    }
  }

  /**
   * Writes the content to the response if available. Delegates to
   * `createNew` if `page` is [[scala.None]]
   *
   * @param page
   * @param resp
   */
  def writePage(page: Option[Content], resp: HttpServletResponse) {
    val out = resp.getOutputStream
    val path = PubletWebContext.applicationPath
    page match {
      case None => createNew(path, resp)
      case Some(p:ErrorResponse) => {
        p.send(resp)
      }
      case Some(p: StreamResponse) => {
        p.send(resp)
      }
      case Some(p) => {
        resp.setContentType(p.contentType.mimeString)
        p.copyTo(out)
      }
    }
  }

  def createNew(path: Path, resp: HttpServletResponse) {
    PubletWeb.notFoundHandler.resourceNotFound(path, resp)
  }

}
