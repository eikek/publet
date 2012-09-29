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

import org.eknet.publet.web.asset.{Kind, AssetManager}
import org.eknet.publet.vfs.{Writeable, Path}
import org.eknet.publet.Publet
import java.util.concurrent
import concurrent.ConcurrentHashMap
import scala.actors.Futures._
import actors.Future
import org.apache.shiro.crypto.hash.Md5Hash
import grizzled.slf4j.Logging
import org.apache.shiro.util.ByteSource
import org.eknet.publet.web.asset.Kind.KindVal
import Path._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.09.12 22:09
 */
class DefaultAssetManager(publet: Publet) extends GroupRegistry with Logging with AssetManager {

  private val fileCache: concurrent.ConcurrentMap[Key, Future[List[Path]]] = new ConcurrentHashMap()

  def getCompressed(group: String, path: Path, kind: Kind.KindVal): Path =  {
    val newTask = future {
      val sources = getSources(group, path, kind)
      val bytes = ConcatInputStream(sources.map(_.inputStream))
      // create filename
      val fileName = new Md5Hash(ByteSource.Util.bytes(bytes)).toHex + "."+ kind.toString
      // if file does not exists, create it
      val targetPath = AssetManager.compressedPath.p / fileName
      publet.rootContainer.lookup(targetPath) getOrElse {
        val target = publet.rootContainer.createResource(targetPath)
        kind.processor.createResource(sources, target.asInstanceOf[Writeable])
      }
      List(targetPath)
    }
    val task = fileCache.putIfAbsent(Key(group, path, kind, compressed = true), newTask)
    val list = if (task == null) {
      newTask()
    } else {
      task()
    }
    list(0)
  }

  def getResources(group: String, path: Path, kind: KindVal) = {
    val newTask = future {
      val sources = getSources(group, path, kind)
      for (source <- sources) yield {
        val targetPath = AssetManager.partsPath.p / source.name
        val target = publet.rootContainer.createResource(targetPath)
        target.asInstanceOf[Writeable].writeFrom(source.inputStream)
        targetPath
      }
    }
    val task = fileCache.putIfAbsent(Key(group, path, kind, compressed = false), newTask)
    if (task == null) {
      newTask()
    } else {
      task()
    }
  }

  private case class Key(group: String, path: Path, kind: Kind.KindVal, compressed: Boolean)
}
