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

package org.eknet.publet.partition.git

import org.eknet.publet.vfs.fs.FileResource
import java.io.{OutputStream, InputStream, File}
import scala.Option
import org.eknet.publet.vfs.{ChangeInfo, Content, Path}

class GitFile(f: File,
              rootPath: Path,
              gp: GitPartition) extends FileResource(f, rootPath) with GitFileTools {
  override def delete() {
    super.delete()
    gp.commitDelete(this)
  }


  protected def root = gp

  override def writeFrom(in: InputStream, changeInfo: Option[ChangeInfo] = None) {
    Content.copy(in, new OutStream(super.outputStream, changeInfo), closeIn = false)
  }

  override def outputStream: OutputStream = {
    new OutStream(super.outputStream)
  }

  override protected def newDirectory(f: File, root: Path) = GitPartition.newDirectory(f, root, gp)

  override protected def newFile(f: File, root: Path) = GitPartition.newFile(f, root, gp)

  def lastAuthor = {
    val commit = lastCommit
    commit map (_.getAuthorIdent)
  }

  private class OutStream(out:OutputStream, changeInfo: Option[ChangeInfo] = None) extends OutputStream {

    def write(b: Int) {
      out.write(b)
    }

    override def write(b: Array[Byte]) {
      out.write(b)
    }

    override def write(b: Array[Byte], off: Int, len: Int) {
      out.write(b, off, len)
    }

    override def close() {
      out.close()
      gp.commitWrite(GitFile.this, changeInfo)
    }

    override def flush() {
      out.flush()
    }
  }
}
