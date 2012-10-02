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

package org.eknet.publet.ext.thumb

import org.eknet.publet.web.{PubletWebContext, PubletWeb, PubletRequestWrapper, EmptyExtension}
import javax.servlet.http.HttpServletRequest
import org.eknet.publet.web.scripts.WebScriptResource
import org.eknet.publet.vfs.util.MapContainer
import org.eknet.publet.vfs.Path._
import org.eknet.publet.web.filter.Filters
import org.eknet.publet.vfs.ContentResource

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.10.12 20:46
 */
class ThumbnailExtension extends EmptyExtension with PubletRequestWrapper {

  override def onStartup() {
    import org.eknet.publet.vfs.ResourceName._
    val muc = new MapContainer()
    muc.addResource(new WebScriptResource("thumb.png".rn, new ThumbnailScript))
    PubletWeb.publet.mountManager.mount("/publet/ext/thumbnail/".p, muc)
  }

  /**
   * Forward all requests with a `thumb` request parameter to the
   * thumbnail script.
   *
   * @param req
   * @return
   */
  override def onBeginRequest(req: HttpServletRequest) = {
    val publet = PubletWeb.publet
    PubletWebContext.param("thumb") match {
      case Some(x) => {
        publet.rootContainer.lookup(PubletWebContext.applicationPath)
          .collect({case c:ContentResource if (c.contentType.mime._1 == "image") => c})
          .map(image => {

          PubletWebContext.requestMap.put(ThumbnailScript.imageResource, image)
          Filters.forwardRequest("/publet/ext/thumbnail/thumb.png".p, false)
        }).getOrElse(req)
      }
      case None => req
    }
  }
}
