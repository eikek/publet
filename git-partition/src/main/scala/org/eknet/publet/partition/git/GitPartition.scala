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

/**
 *
 * @param base
 * @param reponame
 * @param pollInterval the bare repository is polled frequently to integrate the
 *                     latest changes into the workspace
 */
class GitPartition (
      val base: File,
      reponame: String,
      pollInterval: Int
) extends FilesystemPartition(new File(base, reponame+"_wc"), false) with Logging {

  // the working copy is checked out to $base/$reponame_wc while the bare repo is at $base/$reponame.git

  private val bareRepo = {
    val d = new File(base, reponame +".git")
    if (!d.exists()) {
      val r = Git.init().setBare(true).setDirectory(d).call().getRepository
      info("Created bare repository at: "+ r.getDirectory)
      r
    } else {
      val r = new FileRepositoryBuilder()
        .setBare()
        .setGitDir(d)
        .readEnvironment()
        .build()
      info("Using bare repository at: "+ r.getDirectory)
      r
    }
  }

  private val workspaceRepo = {
    if (!root.exists()) {
      val r = Git.cloneRepository()
        .setBare(false)
        .setCloneAllBranches(true)
        .setURI(bareRepo.getDirectory.toURI.toString)
        .setDirectory(root)
        .call().getRepository

      val cfg = r.getConfig
      cfg.setString("branch", "master", "remote", "origin")
      cfg.setString("branch", "master", "merge", "refs/heads/master")
      cfg.setBoolean("http", null, "receivepack", true)
      cfg.save()

      info("Created workspace at: "+ r.getWorkTree)
      r
    } else {
      val r = new FileRepositoryBuilder()
        .setWorkTree(root)
        .readEnvironment()
        .build();
      info("Using workspace at: "+ r.getWorkTree +" with git dir: "+ r.getDirectory)
      r
    }
  }


  private val git = new Git(workspaceRepo)

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

  def head = workspaceRepo.resolve("HEAD")

  private def push() {
    git.push()
      .setRemote("origin")
      .setRefSpecs(new RefSpec("master"))
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


  def close() {
    info("Close git partition at: "+ workspaceRepo.getWorkTree)
    workspaceRepo.close()
    bareRepo.close()
  }

  lazy val repository = bareRepo.getDirectory

  override protected def newDirectory(f: File, root: Path) = GitPartition.newDirectory(f, root, this)
  override protected def newFile(f: File, root: Path) = GitPartition.newFile(f, root, this)

}

object GitPartition {

  def newDirectory(f: File, root: Path, gp: GitPartition): ContainerResource = new GitDirectory(f, root, gp)
  def newFile(f: File, root: Path, gp: GitPartition): ContentResource = new GitFile(f, root, gp)

}
