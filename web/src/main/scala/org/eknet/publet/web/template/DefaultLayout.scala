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

import org.eknet.publet.vfs.Path
import org.eknet.publet.vfs.util.ClasspathContainer
import grizzled.slf4j.Logging
import org.eknet.publet.web.asset.{AssetCollection, AssetManager, Group}
import Path._
import org.eknet.publet.web.template.DefaultLayout.Assets
import org.eknet.publet.Publet
import com.google.inject.{Inject, Singleton}
import org.eknet.publet.web.guice.PubletStartedEvent
import com.google.common.eventbus.Subscribe
import org.eknet.publet.engine.scalate.ScalateEngine

/**
 * Registers provided default assets and sets the default layout template.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 18:25
 */
@Singleton
class DefaultLayout @Inject() (publet: Publet, assetMgr: AssetManager, scalateEngine: ScalateEngine) extends Logging {

  @Subscribe
  def onStartup(ev: PubletStartedEvent) {
    //mustache
    assetMgr setup Assets.mustache

    //jquery
    assetMgr setup (Assets.jquery, Assets.jqueryMigrate)

    //highlightJs
    publet.mountManager.mount("/publet/highlightjs/".p,
      new ClasspathContainer(base = "/org/eknet/publet/web/includes/highlight"))
    assetMgr setup Assets.highlightjs

    //publet's resources
    publet.mountManager.mount("/publet/".p,
      new ClasspathContainer(base = "/org/eknet/publet/web/includes/publet"))
    assetMgr setup Assets.publet

    //jquery.loadmask.spin
    assetMgr setup (Assets.spin, Assets.loadmask)

    //bootstrap
    publet.mountManager.mount(Path("/publet/bootstrap/"),
      new ClasspathContainer(base = "/org/eknet/publet/web/includes/bootstrap"))
    assetMgr setup Assets.bootstrap

    scalateEngine.setDefaultLayoutUri("/publet/bootstrap/bootstrap.single.jade")

    //add default asset groups
    assetMgr.setup(Assets.default, Assets.defaultNoHighlightJs)
  }
}

object DefaultLayout {

  object Assets extends AssetCollection {

    override def classPathBase = "/org/eknet/publet/web/includes"

    val mustache = Group("mustache")
      .add(resource("mustache/mustache.js"))

    val jquery = Group("jquery")
      .add(resource("jquery/jquery-1.9.1.min.js").noCompress)
      .add(resource("jquery/jquery.form.js"))

    val jqueryMigrate = Group("jquery.migrate")
      .add(resource("jquery/jquery-migrate-1.1.1.min.js").noCompress)
      .require(jquery.name)

    val spin =  Group("spinjs").add(resource("spin/spin.min.js").noCompress)

    val loadmask = Group("loadmask.jquery")
      .add(resource("loadmask/jquery.loadmask.spin.js"))
      .add(resource("loadmask/jquery.loadmask.spin.css"))
      .require(jquery.name, spin.name)

    val publet = Group("publet")
      .add(resource("publet/js/publet.js"))
      .add(resource("publet/js/jquery.feedback-message.js"))
      .require(jquery.name)

    val highlightjs = Group("highlightjs")
      .add(resource("highlight/highlight.pack.js").noCompress)
      .add(resource("highlight/highlight-onload.js"))

    val bootstrap = Group("bootstrap")
      .add(resource("bootstrap/js/bootstrap.js"))
      .add(resource("bootstrap/css/bootstrap.css"))
      .add(resource("bootstrap/css/bootstrap.custom.css"))
      .add(resource("bootstrap/img/glyphicons-halflings.png"))
      .add(resource("bootstrap/img/glyphicons-halflings-white.png"))
      .require(jquery.name)

    val default = Group("default").use(mustache.name, jquery.name, spin.name, loadmask.name,
      publet.name, highlightjs.name, bootstrap.name)

    val defaultNoHighlightJs = Group("defaultNoHighlightJs")
      .use(mustache.name, jquery.name, spin.name, loadmask.name, publet.name, bootstrap.name)

  }

}