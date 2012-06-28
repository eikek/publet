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

import org.eknet.publet.vfs._
import java.io.File
import fs.FilesystemPartition
import scala.Option
import org.apache.shiro.{ShiroException, SecurityUtils}
import grizzled.slf4j.Logging
import org.eknet.publet.gitr.Tandem
import org.eknet.publet.auth.{UserProperty, User}

class GitPartition (val tandem: Tandem)
  extends FilesystemPartition(tandem.workTree.getWorkTree, false) with Logging {

  def updateWorkspace():Boolean = {
    val result = tandem.updateWorkTree()
    result.isSuccessful
  }

  private def getCurrentUser = {
    try {
      Option(SecurityUtils.getSubject.getPrincipal) flatMap { p =>
        if (p.isInstanceOf[User]) Some(p.asInstanceOf[User])
        else None
      }
    } catch {
      case e: ShiroException => None
    }
  }

  private def git = tandem.workTree.git

  private def commit(c: GitFile, action:String) {
    val user = getCurrentUser
    val name = user.flatMap(_.getProperty(UserProperty.fullName)).getOrElse("Publet Git")
    val email = user.flatMap(_.getProperty(UserProperty.email)).getOrElse("no@none.com")
    val message = action +"\n\nresource: "+ c.name.fullName+"\nsubject: "+user.map(_.login).getOrElse("anonymous")
    git.commit()
      .setMessage(message)
      .setAuthor(name, email)
      .setAll(true)
      .call()
  }

  protected[git] def commitWrite(c: GitFile, message: Option[String] = None) {
    val path = Path(c.file).strip(c.rootPath)
    git.add()
      .addFilepattern(path.toRelative.asString)
      .setUpdate(false)
      .call()

    if (!git.status().call().isClean) {
      info("commit: "+ path.toRelative.asString)

      commit(c, message.getOrElse("Update"))
      tandem.pushToBare()
    }
  }

  protected[git] def commitDelete(c: GitFile) {
    val path = Path(c.file).strip(c.rootPath)
    git.rm()
      .addFilepattern(path.toRelative.asString)
      .call()
    commit(c, "Delete")
    tandem.pushToBare()
  }

  override def children = super.children.filterNot(_.name.name == ".git/")

  override protected def newDirectory(f: File, root: Path) = GitPartition.newDirectory(f, root, this)
  override protected def newFile(f: File, root: Path) = GitPartition.newFile(f, root, this)

}

object GitPartition {

  private val mountPointProperty = "publetMountPoint"

  def newDirectory(f: File, root: Path, gp: GitPartition) = new GitDirectory(f, root, gp)
  def newFile(f: File, root: Path, gp: GitPartition) = new GitFile(f, root, gp)

}
