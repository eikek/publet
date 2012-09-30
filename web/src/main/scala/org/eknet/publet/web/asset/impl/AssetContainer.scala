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

import org.eknet.publet.vfs._
import fs.FilesystemPartition
import org.eknet.publet.web.asset._
import org.eknet.publet.vfs.Path._
import org.eknet.publet.vfs.util.MapContainer
import java.io.File
import org.eknet.publet.web.asset.AssetResource
import scala.Some

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.09.12 14:25
 */
class AssetContainer(tempDir: File) extends MountManager with RootContainer {

  mount("compressed".p, new FilesystemPartition(tempDir, true))

  def mount(g: Group) {
    g.resources.foreach(mount)
  }

  def mount(r: AssetResource) {
    val path = internalPath(r)
    mountTo(path, r)

    val kinds = Kind.values.map(_.asInstanceOf[Kind.KindVal].ext)
    if (!kinds.contains(r.name.ext)) {
      val p = defaultFolder(r).p / r.name
      mountTo(p, r)
    }
  }
  private def mountTo(path: Path, r: AssetResource) {
    resolveMount(path) match {
      case Some(part) => {
        part._2.asInstanceOf[MapContainer].addResource(r)
      }
      case None => {
        val c = new MapContainer
        mount(path.parent, c)
        c.addResource(r)
      }
    }
  }

  private def internalPath(r: AssetResource) =
    "groups".p / r.group / defaultFolder(r) / r.name

  private def internalTemp(name: String) = AssetManager.compressedPath.p.segments.last.p / name

  def pathFor(r: AssetResource) =
    AssetManager.assetPath.p / internalPath(r)

  def pathForCompressed(name: String) = AssetManager.compressedPath.p / name

  def lookupTempFile(name: String) = lookup(internalTemp(name))

  def createTempFile(name: String) = createResource(internalTemp(name))

  private def defaultFolder(r: AssetResource) = {
    r.name.targetType match {
      case ContentType.javascript => "js"
      case ContentType.css => "css"
      case m if (m.mime._1 == "image") => "img"
      case _ => "other"
    }
  }
}
