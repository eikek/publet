package org.eknet.publet.partition.git

import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.RefSpec
import org.eknet.publet.vfs._
import java.io.File
import fs.FilesystemPartition
import scala.Option
import org.eknet.publet.auth.User
import org.apache.shiro.{ShiroException, SecurityUtils}
import grizzled.slf4j.Logging
import org.eclipse.jgit.lib.Repository

class GitPartition (bareRepo: Repository, wsRepo: Repository)
  extends FilesystemPartition(wsRepo.getWorkTree, false) with Logging {

  private val git = new Git(wsRepo)

  def updateWorkspace():Boolean = {
    val result = git.pull().call()
    result.isSuccessful
  }

  def lastCommit(gf: GitFile) = {
    val path = gf.file.getAbsolutePath.substring(git.getRepository.getWorkTree.getAbsolutePath.length()+1)
    var logs = git.log().addPath(path).call().iterator()
    if (logs.hasNext)
      Some(logs.next())
    else
      None
  }
  def lastCommit(path: Path) = {
    var logs = git.log().addPath(path.toRelative.asString).call().iterator()
    if (logs.hasNext)
      Some(logs.next())
    else
      None
  }

  def head = wsRepo.resolve("HEAD")

  private def push() {
    git.push()
      .call()
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

  private def commit(c: GitFile, action:String) {
    val user = getCurrentUser
    val name = user.map(_.fullname).getOrElse("Publet Git")
    val email = user.map(_.email).getOrElse("no@none.com")
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
      push()
    }
  }

  protected[git] def commitDelete(c: GitFile) {
    val path = Path(c.file).strip(c.rootPath)
    git.rm()
      .addFilepattern(path.toRelative.asString)
      .call()
    commit(c, "Delete")
    push()
  }

  lazy val repository = bareRepo.getDirectory


  override def children = super.children.filterNot(_.name.name == ".git/")

  override protected def newDirectory(f: File, root: Path) = GitPartition.newDirectory(f, root, this)
  override protected def newFile(f: File, root: Path) = GitPartition.newFile(f, root, this)

}

object GitPartition {

  def newDirectory(f: File, root: Path, gp: GitPartition) = new GitDirectory(f, root, gp)
  def newFile(f: File, root: Path, gp: GitPartition) = new GitFile(f, root, gp)

}
