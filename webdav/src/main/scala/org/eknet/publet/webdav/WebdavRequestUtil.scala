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

package org.eknet.publet.webdav

import javax.servlet.http.HttpServletRequest
import org.eknet.publet.web._
import org.eknet.publet.web.util.Key
import scala.Some

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.09.12 22:54
 */
trait WebdavRequestUtil {
  this: RequestAttr with RequestUrl =>

  protected def req: HttpServletRequest

  private val isWebdavRequestKey = Key(getClass.getName, {
      case org.eknet.publet.web.util.Request => {
        //for windows clients: they probe the server with an OPTIONS request to the root
        //thus, we should let this go to milton.
        isDavRequest(applicationUri) || (applicationPath.isRoot && req.getMethod == Method.options.toString)
      }
    })


  /**
   * Returns whether the current request is handled by the webdav filter
   *
   * @return
   */
  def isDavRequest: Boolean = attr(isWebdavRequestKey).get

  /**
   * Returns whether the request is pointing to a resource that
   * is mounted as webdav resource.
   *
   * @param path the request uri path
   * @return
   */
  private def isDavRequest(path: String): Boolean = {
    if (!Config("webdav.enabled").map(_.toBoolean).getOrElse(true)) {
      false
    } else {
      getWebdavFilterUrls.exists(url => path.startsWith(url))
    }
  }

  /**
   * Returns all configured url prefixes that are handled by the
   * webdav filter.
   *
   * @return
   */
  private def getWebdavFilterUrls = {
    def recurseFind(num: Int): List[String] = {
      val key = "webdav.filter."+num
      PubletWeb.publetSettings(key) match {
        case Some(filter) => {
          filter :: recurseFind(num +1)
        }
        case None => Nil
      }
    }
    recurseFind(0)
  }
}
