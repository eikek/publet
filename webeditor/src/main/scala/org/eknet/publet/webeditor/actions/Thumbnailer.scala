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

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.{PubletWeb, PubletWebContext}
import org.eknet.publet.vfs.{Content, ContentResource, Path}
import javax.imageio.ImageIO
import java.io.ByteArrayOutputStream
import org.eknet.publet.webeditor.WebEditor

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 24.05.12 12:20
 */
object Thumbnailer extends ScalaScript {

  def serve() = {
    val publet = PubletWeb.publet
    val p = PubletWebContext.param("resource").map(Path(_))
    PubletWebContext.param("resource")
      .map(Path(_))
      .filter(_.name.targetType.mime._1 == "image")
      .map(publet.rootContainer.lookup)
      .collect({case Some(c: ContentResource)=>c})
      .map(scale)
      .orElse(loadNoPreview())
  }

  def scale(c: ContentResource): Content = {
    import ScalaScript._
    val maxh = PubletWebContext.param("maxh").getOrElse("45").toInt
    val maxw = PubletWebContext.param("maxw").getOrElse("80").toInt
    val img = ImageIO.read(c.inputStream)
    val scaled = ImageScaler.scaleIfNecessary(img, maxw, maxh)
    val baos = new ByteArrayOutputStream()
    ImageIO.write(scaled, "PNG", baos)
    makePng(baos.toByteArray).get
  }

  def loadNoPreview(): Option[Content] = {
    import ScalaScript._

    val resource = "includes/img/publet_nopreview.png"
    makePng(classOf[WebEditor].getResourceAsStream(resource))
  }
}
