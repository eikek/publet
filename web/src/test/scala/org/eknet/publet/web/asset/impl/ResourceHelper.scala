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

package org.eknet.publet.web.asset.impl

import org.eknet.publet.web.asset.{AssetResource, AssetCollection, Group}
import org.eknet.publet.vfs.{ContentResource, ResourceName}
import java.io.ByteArrayInputStream

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.09.12 16:31
 */
object ResourceHelper extends AssetCollection {

  override def classPathBase = "/org/eknet/publet/web/includes"

  val bootstrapGroup = Group("bootstrap")
    .add(resource("bootstrap/js/bootstrap.js").into("js")) // into("js") is default for js
    .add(resource("bootstrap/css/bootstrap.css").into("css")) //into("css") is default for css
    .add(resource("bootstrap/css/bootstrap.custom.css"))
    .add(resource("bootstrap/img/glyphicons-halflings.png").into("img")) //into("img") is default for images
    .add(resource("bootstrap/img/glyphicons-halflings-white.png"))
//    .require("jquery")

  val highlightGroup = Group("highlightjs")
    .add(resource("highlight/highlight.pack.js").noCompress)
    .add(resource("highlight/styles/googlecode.css"))

  val jqueryGroup = Group("jquery")
    .add(resource("jquery/jquery-1.8.2.min.js").noCompress)
    .add(resource("jquery/jquery.form.js"))

  val spinGroup = Group("spinjs")
    .add(resource("spin/spin.min.js"))

  val loadmaskGroup = Group("jquery.loadmask")
    .add(resource("loadmask/jquery.loadmask.spin.js"))
    .add(resource("loadmask/jquery.loadmask.spin.css"))
    .require("jquery", "spinjs")

  val publetGroup = Group("publet")
    .add(resource("publet/js/publet.js"))
    .require("jquery")

  def mockResource(name: String):AssetResource = NameResource(name)

  case class NameResource(n: String) extends ContentResource {
    def name = ResourceName(n)
    def exists = true
    def contentType = name.targetType
    def inputStream = new ByteArrayInputStream(Array())
    override def toString = n
  }
}
