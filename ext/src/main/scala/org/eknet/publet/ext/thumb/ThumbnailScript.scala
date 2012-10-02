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

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.{PubletWeb, PubletWebContext}
import org.eknet.publet.web.util.Key
import org.eknet.publet.vfs.Path._
import org.eknet.publet.vfs.ContentResource

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.10.12 23:09
 */
class ThumbnailScript extends ScalaScript {
  import ThumbnailScript._

  def serve() = {
    val image = PubletWebContext.attr(imageResource).orElse(
      PubletWeb.publet.rootContainer.lookup(getResourcePath)
      .collect({case c: ContentResource=>c}))


    image.flatMap(img => {
      val thumb = Thumbnailer.service().thumbnail(img, getMaxHeight, getMaxWidth)
      PubletWeb.publet.rootContainer.lookup(thumb)
        .collect({case c:ContentResource => c})
    })
  }

  def getMaxHeight = PubletWebContext.param("maxh").map(_.toInt)
    .orElse(PubletWebContext.attr(maxHeight))
    .getOrElse(45)

  def getMaxWidth = PubletWebContext.param("maxw").map(_.toInt)
    .orElse(PubletWebContext.attr(maxWidth))
    .getOrElse(80)

  def getResourcePath = PubletWebContext.param("resource")
    .orElse(PubletWebContext.attr(resourcePath))
    .getOrElse(sys.error("No resource given to thumbnailer")).p

}

object ThumbnailScript {
  val maxHeight = Key[Int]("thumbnail.maxHeight")
  val maxWidth = Key[Int]("thumbnail.maxWidht")
  val resourcePath = Key[String]("thumbnail.resourcePath")

  val imageResource = Key[ContentResource]("thumbnail.imageResource")
}
