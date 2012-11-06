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

package org.eknet.publet.gitr.partition

import org.eknet.publet.vfs.fs.FileResource
import java.io.{OutputStream, InputStream, File}
import scala.Option
import org.eknet.publet.vfs.{ChangeInfo, Content, Path}
import com.google.common.eventbus.EventBus

class GitFile(f: File,
              rootPath: Path,
              val gp: GitPartition) extends FileResource(f, rootPath, gp.bus) with GitFileTools {

  protected def root = gp

  override protected def newDirectory(f: File, root: Path, bus: EventBus) = GitPartition.newDirectory(f, root, gp)

  override protected def newFile(f: File, root: Path, bus: EventBus) = GitPartition.newFile(f, root, gp)

  def lastAuthor = {
    val commit = lastCommit
    commit map (_.getAuthorIdent)
  }

  def commitWrite(changeInfo: Option[ChangeInfo]) {
    gp.commitWrite(this, changeInfo)
  }

  def commitDelete() {
    gp.commitDelete(this)
  }
}
