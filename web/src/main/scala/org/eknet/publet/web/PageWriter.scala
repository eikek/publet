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

import filter.ServeContentResource
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.eknet.publet.vfs._
import scala.Some
import grizzled.slf4j.Logging
import org.eknet.publet.Publet
import util.{StringMap, RenderUtils}
import org.eknet.publet.vfs.util.SimpleContentResource

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
  def writeError(code: Int, req: HttpServletRequest, resp: HttpServletResponse) {
    writePage(Some(getCustomErrorPage(code).getOrElse(ErrorResponse(code))), req, resp)
  }

  /**
   * In development mode, writes the exception to the page. Otherwise
   * delegates to `writeError` for rendering an error page.
   * @param ex
   * @param resp
   */
  def writeError(ex: Throwable, req: HttpServletRequest, resp: HttpServletResponse) {
    if (Config.mode == "development") {
      //print the exception in development mode
      try {
        writePage(RenderUtils.renderException(ex), req, resp)
      }
      catch {
        case e:Throwable => {
          error("Error while writing application exception!", e)
          writeError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, req, resp)
        }
      }
    } else {
      writeError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, req, resp)
    }
  }

  /**
   * Writes the content to the response if available. Delegates to
   * `createNew` if `page` is [[scala.None]]
   *
   * @param page
   * @param resp
   */
  def writePage(page: Option[Content], req: HttpServletRequest, resp: HttpServletResponse) {
    val path = PubletWebContext.applicationPath
    page match {
      case None => createNew(path, req, resp)
      case Some(p: CustomContent) => p.send(req, resp)
      case Some(p) => ServeContentResource.serveResource(new SimpleContentResource(path.name, p), req, resp)
    }
  }

  def createNew(path: Path, req: HttpServletRequest, resp: HttpServletResponse) {
    val notFoundHandler = {
      val settings = PubletWeb.instance[StringMap]("settings")
      settings("publet.service.notFoundHandlerNamed").map { name =>
        PubletWeb.instance[NotFoundHandler](name)
      } getOrElse {
        PubletWeb.instance[NotFoundHandler]
      }
    }
    notFoundHandler.resourceNotFound(path, req, resp)
  }

}
