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

package org.eknet.publet.web.asset

import impl.DefaultAssetManager
import org.eknet.publet.web._
import util.Key
import java.io.IOException
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import org.eknet.publet.vfs.fs.FilesystemPartition
import org.eknet.publet.vfs.Path._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.09.12 18:55
 */
class AssetExtension extends EmptyExtension {

  override def onShutdown() {
    PubletWeb.contextMap(AssetExtension.tempDirKey) map  { tempDir =>
      Files.walkFileTree(tempDir, new SimpleFileVisitor[Path] {
        override def visitFile(file: Path, attrs: BasicFileAttributes) = {
          Files.delete(file)
          FileVisitResult.CONTINUE
        }
        override def postVisitDirectory(dir: Path, exc: IOException) = {
          if (exc == null) {
            Files.delete(dir)
            FileVisitResult.CONTINUE
          } else {
            FileVisitResult.TERMINATE
          }
        }
      })
    }
  }
}

object AssetExtension {

  private val tempDirKey = Key(classOf[AssetManager].getName+ "::TempDirectory")
  private val assetManagerKey = Key[AssetManager](classOf[AssetManager].getName)

  def assetManager = PubletWebContext.contextMap(assetManagerKey).getOrElse {
    val tempDir = Config.newTempDir("assets")
    PubletWeb.contextMap.put(tempDirKey, tempDir)
    PubletWeb.publet.mountManager.mount(AssetManager.assetPath.p,
      new FilesystemPartition(tempDir, true))

    val mgr: AssetManager = new DefaultAssetManager(PubletWeb.publet)
    PubletWeb.contextMap.put(assetManagerKey, mgr)
    mgr
  }
}
