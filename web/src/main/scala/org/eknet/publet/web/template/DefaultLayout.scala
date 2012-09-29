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

package org.eknet.publet.web.template

import org.eknet.publet.vfs.{Content, ContentType, Path}
import org.eknet.publet.web.{EmptyExtension, PubletWeb}
import org.eknet.publet.vfs.util.{SimpleContentResource, UrlResource, ClasspathContainer}
import grizzled.slf4j.Logging
import org.eknet.publet.web.asset.{Group, AssetExtension}
import Path._
import org.eknet.publet.web.template.DefaultLayout.Assets
import io.Source

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 18:25
 */
class DefaultLayout extends EmptyExtension with Logging {

  override def onStartup() {
    val publ = PubletWeb.publet
    val assetMgr = AssetExtension.assetManager

    //jquery
    assetMgr setup Assets.jquery

    //highlightJs
    publ.mountManager.mount("/publet/highlightjs/".p,
      new ClasspathContainer(base = "/org/eknet/publet/web/includes/highlight"))
    assetMgr setup Assets.highlightjs

    //publet's resources
    publ.mountManager.mount("/publet/".p,
      new ClasspathContainer(base = "/org/eknet/publet/web/includes/publet"))
    assetMgr setup Assets.publet

    //jquery.loadmask.spin
    assetMgr setup (Assets.spin, Assets.loadmask)

    //bootstrap
    publ.mountManager.mount(Path("/publet/bootstrap/"),
      new ClasspathContainer(base = "/org/eknet/publet/web/includes/bootstrap"))
    assetMgr setup Assets.bootstrap

    PubletWeb.scalateEngine.setDefaultLayoutUri("/publet/bootstrap/bootstrap.single.jade")

    //add default asset groups
    assetMgr.setup(Assets.default, Assets.defaultNoHighlightJs)
  }
}

object DefaultLayout {
  import org.eknet.publet.vfs.ResourceName._
  object Assets {

    val jquery = Group("jquery")
      .add(resource("jquery/jquery-1.8.2.min.js"))
      .add(resource("jquery/jquery.form.js"))

    val spin =  Group("spinjs").add(resource("spin/spin.min.js"))

    val loadmask = Group("loadmask.jquery")
      .add(resource("loadmask/jquery.loadmask.spin.js"))
      .add(resource("loadmask/jquery.loadmask.spin.css"))
      .require(jquery.name, spin.name)

    val publet = Group("publet")
      .add(resource("publet/js/publet.js"))
      .require(jquery.name)

    val highlightjs = Group("highlightjs")
      .add(resource("highlight/highlight.pack.js"))
      .add(resource("highlight/highlight-onload.js"))

    val bootstrap = Group("bootstrap")
      .add(resource("bootstrap/js/bootstrap.js"))
      .add(new SimpleContentResource("bootstrap.css".rn, Content(replaceBootstrapImgUrl, ContentType.css)))
      .add(resource("bootstrap/css/bootstrap.custom.css"))
      .require(jquery.name)

    val default = Group("default").use(jquery.name, spin.name, loadmask.name,
      publet.name, highlightjs.name, bootstrap.name)

    val defaultNoHighlightJs = Group("defaultNoHighlightJs")
      .use(jquery.name, spin.name, loadmask.name, publet.name, bootstrap.name)
  }

  private def findResource(name: String) = Option(getClass.getResource(
    ("/org/eknet/publet/web/includes".p / name).asString))

  private def resource(name: String) = new UrlResource(findResource(name).get)

  private def replaceBootstrapImgUrl: String = {
    val file = findResource("bootstrap/css/bootstrap.css").get
    Source.fromURL(file, "UTF-8").getLines().map(line => {
      if (line.contains("../img/"))
        line.replace("../img/", "../bootstrap/img/")
      else
        line
    }).mkString("\n")
  }
}