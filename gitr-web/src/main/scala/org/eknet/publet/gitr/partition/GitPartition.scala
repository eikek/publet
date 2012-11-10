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

import org.eknet.publet.vfs._
import java.io.File
import fs.FilesystemPartition
import scala.Option
import org.apache.shiro.SecurityUtils
import grizzled.slf4j.Logging
import com.google.common.eventbus.{Subscribe, EventBus}
import org.eknet.gitr.Tandem
import org.eknet.publet.auth.{ResourceAction, Authorizable}
import org.eknet.publet.auth.ResourceAction.Action
import org.eknet.publet.gitr.auth.{GitPermissionBuilder, RepositoryTag, DefaultRepositoryStore}
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.gitr.auth.GitAction._
import org.eknet.publet.vfs.ChangeInfo
import org.eknet.publet.auth.ResourceAction.Action
import scala.Some
import org.eknet.publet.gitr.PostReceiveEvent
import org.eknet.publet.vfs.events.ContainerModifiedEvent

class GitPartition (val tandem: Tandem, val bus: EventBus, repoStore: DefaultRepositoryStore)
  extends FilesystemPartition(tandem.workTree.getWorkTree, bus, false) with Authorizable with GitPermissionBuilder with Logging {

  bus.register(this)

  @Subscribe
  def emitContainerModified(event: PostReceiveEvent) {
    bus.post(new ContainerModifiedEvent(this))
  }

  def updateWorkspace():Boolean = {
    tandem.updateWorkTree()
    true
  }

  def isAuthorized(action: Action) = {
    action match {
      case ResourceAction.all => hasRead && hasWrite
      case ResourceAction.read => hasRead
      case ResourceAction.write => hasWrite
    }
  }

  private def hasRead = {
    val name = tandem.bare.name
    val model = repoStore.getRepository(name)
    if (model.tag == RepositoryTag.open)
      true
    else {
      Security.hasPerm(git action(pull) on(name))
    }
  }

  private def hasWrite = {
    val name = tandem.bare.name
    Security.hasPerm(git action(push) on(name))
  }


  private def workTree = tandem.workTree.git

  private def commit(c: GitFile, changeInfo: Option[ChangeInfo], action:String) {
    val login = Option(SecurityUtils.getSubject.getPrincipal).map(_.toString).getOrElse("anonymous")
    val name = changeInfo.flatMap(_.name).getOrElse("Publet Git")
    val email = changeInfo.flatMap(_.email).getOrElse("no@none.com")
    val message = (changeInfo.map(_.message) match {
      case Some(m) if (!m.isEmpty) => m
      case _ => action
    }) + "\n\nresource: "+ c.name.fullName+"\nsubject: "+ login

    workTree.commit()
      .setMessage(message)
      .setAuthor(name, email)
      .setAll(true)
      .call()
  }

  protected[gitr] def commitWrite(c: GitFile, changeInfo: Option[ChangeInfo] = None) {
    synchronized {
      val path = Path(c.file).strip(c.rootPath)
      workTree.add()
        .addFilepattern(path.toRelative.asString)
        .setUpdate(false)
        .call()

      if (!workTree.status().call().isClean) {
        info("commit: "+ path.toRelative.asString)

        commit(c, changeInfo, "Update")
        tandem.pushToBare()
      }
    }
  }

  protected[gitr] def commitDelete(c: GitFile) {
    synchronized {
      val path = Path(c.file).strip(c.rootPath)
      workTree.rm()
        .addFilepattern(path.toRelative.asString)
        .call()
      commit(c, None, "Delete")
      tandem.pushToBare()
    }
  }

  override def children = super.children.filterNot(_.name.name == ".git/")

  override protected def newDirectory(f: File, root: Path, bus: EventBus) = GitPartition.newDirectory(f, root, this)
  override protected def newFile(f: File, root: Path, bus: EventBus) = GitPartition.newFile(f, root, this)

}

object GitPartition {

  def newDirectory(f: File, root: Path, gp: GitPartition) = new GitDirectory(f, root, gp)
  def newFile(f: File, root: Path, gp: GitPartition) = new GitFile(f, root, gp)

}
