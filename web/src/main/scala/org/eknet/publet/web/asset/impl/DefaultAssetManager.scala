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

import org.eknet.publet.web.asset.{Group, Kind, AssetManager}
import org.eknet.publet.vfs.{Writeable, Path}
import org.eknet.publet.Publet
import java.util.concurrent
import concurrent.ConcurrentHashMap
import org.apache.shiro.crypto.hash.Md5Hash
import grizzled.slf4j.Logging
import org.apache.shiro.util.ByteSource
import org.eknet.publet.web.asset.Kind.KindVal
import Path._
import java.io.File
import com.google.common.eventbus.EventBus
import com.google.common.base.{Suppliers, Supplier}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.09.12 22:09
 */
class DefaultAssetManager(publet: Publet, bus: EventBus, tempDir: File) extends GroupRegistry with Logging with AssetManager {

  private val fileCache: concurrent.ConcurrentMap[Key, Supplier[Path]] = new ConcurrentHashMap()
  private val assetContainer = new AssetContainer(tempDir, bus)

  if (publet.mountManager.resolveMount(AssetManager.assetPath.p) == None) {
    publet.mountManager.mount(AssetManager.assetPath.p, assetContainer)
  }

  override def setup(groups: Group*) {
    super.setup(groups: _*)
    groups.foreach(assetContainer.mount)
  }

  override def replace(groups: Group*): Seq[Group] = {
    val x = super.replace(groups: _*)
    x.foreach(assetContainer.mount)
    x
  }

  def getCompressed(group: Iterable[String], path: Option[Path], kind: Kind.KindVal): Path =  {
    val newTask = Suppliers.memoize(new Supplier[Path] {
      def get() = {
        val sources = getSources(group, path, kind)
        val bytes = ConcatInputStream(sources.map(_.inputStream))
        // create filename
        val fileName = new Md5Hash(ByteSource.Util.bytes(bytes)).toHex + "."+ kind.toString
        // if file does not exists, create it
        assetContainer.lookupTempFile(fileName) getOrElse {
          val target = assetContainer.createTempFile(fileName)
          kind.processor.createResource(sources, target.asInstanceOf[Writeable])
        }
        assetContainer.pathForCompressed(fileName)
      }
    })
    val task = fileCache.putIfAbsent(Key(group, path, kind), newTask)
    if (task != null) task.get() else newTask.get()
  }

  def getResources(group: Iterable[String], path: Option[Path], kind: KindVal) = {
    val sources = getSources(group, path, kind)
    sources map { s => assetContainer.pathFor(s) }
  }

  private case class Key(group: Iterable[String], path: Option[Path], kind: Kind.KindVal)
}
