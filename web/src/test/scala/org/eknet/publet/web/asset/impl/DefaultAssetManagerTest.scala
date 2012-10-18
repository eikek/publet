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

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.eknet.publet.vfs.Path
import Path._
import org.eknet.publet.impl.PubletImpl
import org.eknet.publet.web.asset.{Kind, Group}
import java.io.File
import java.nio.file.Files
import ResourceHelper._
import org.eknet.publet.Publet
import com.google.common.eventbus.EventBus

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.09.12 12:11
 */
class DefaultAssetManagerTest extends FunSuite with ShouldMatchers with BeforeAndAfter {

  var publet: Publet = _
  var assetMgr: DefaultAssetManager = _
  var tempDir: File = _

  implicit def toSome(p:Path) = Some(p)

  before {
    publet = new PubletImpl
    tempDir = Files.createTempDirectory("testassets").toFile
    assetMgr = new DefaultAssetManager(publet, new EventBus(), tempDir)
  }

  after {
    def deleteRec(f: File) {
      if (f.isDirectory) f.listFiles().foreach(deleteRec)
      f.delete()
    }
    deleteRec(tempDir)
  }

  test ("create simple group") {
    assetMgr setup (bootstrapGroup, jqueryGroup)

    val pathCss = assetMgr.getCompressed(Seq("bootstrap"), "/main/index.html".p, Kind.css)
    publet.rootContainer.lookup(pathCss) should  not be None

    val pathJs = assetMgr.getCompressed(Seq("bootstrap"), "/main/".p, Kind.js)
    publet.rootContainer.lookup(pathJs) should  not be None
    println(pathCss.asString + "\n" + pathJs.asString)
  }

  test ("create uses group") {
    assetMgr setup (jqueryGroup, spinGroup, loadmaskGroup, publetGroup, highlightGroup, bootstrapGroup)
    assetMgr setup Group("default").use("bootstrap", "highlightjs", "jquery.loadmask", "publet")

    val pathJs = assetMgr.getCompressed(Seq("default"), "/main/".p, Kind.js)
    publet.rootContainer.lookup(pathJs) should  not be None

    val pathCss = assetMgr.getCompressed(Seq("default"), "/main/index.html".p, Kind.css)
    publet.rootContainer.lookup(pathCss) should  not be None

    println(pathCss.asString + "\n" + pathJs.asString)
  }

  test ("get single resources") {
    assetMgr setup (jqueryGroup, spinGroup, loadmaskGroup, publetGroup, highlightGroup, bootstrapGroup)
    assetMgr setup Group("default").use("bootstrap", "highlightjs", "jquery.loadmask", "publet")

    val js = assetMgr.getResources(Seq("default"), "/main".p, Kind.js)
    println(js.map(_.asString).mkString("; "))
  }
}
