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

package org.eknet.publet.webeditor.actions

import org.eknet.publet.vfs.{ContentResource, Resource, Path}
import org.eknet.publet.webeditor.EditorPaths
import org.eknet.publet.web.util.PubletWebContext

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.10.12 11:45
 */
trait ThumbnailUrlUtil {

  /**
   * Returns a full URL to a preview image of the given resource. If it
   * is an image, an URL to a thumbnail is returned, otherwise some
   * place-holder image is returned.
   *
   * @param path the path to the resource
   * @param r the resource used for checking content type
   * @return
   */
  def thumbnailUrl(path: Path, r: Resource) = {
    val ctx = PubletWebContext
    val qp = "maxw="+getThumbnailWidth+"&maxh="+getThumbnailHeight
    val url = if (r.isInstanceOf[ContentResource]
      && r.asInstanceOf[ContentResource].contentType.mime._1 == "image") {

      ctx.urlOf(path.asString)
    } else {
      ctx.urlOf(EditorPaths.editorPath / "img/nopreview.png")
    }
    url + "?thumb&"+qp
  }

  def getThumbnailHeight = 45
  def getThumbnailWidth = 80

}
