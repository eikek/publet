package org.eknet.publet.partition.git

import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.RefSpec
import org.eknet.publet.vfs._
import java.io.File
import fs.FilesystemPartition
import scala.Option
import org.slf4j.LoggerFactory
import actors.Actor
import Actor._

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
) extends FilesystemPartition(new File(base, reponame+"_wc"), false) {

  private val log = LoggerFactory.getLogger(getClass)

  // the working copy is checked out to $base/$reponame_wc while the bare repo is at $base/$reponame.git

  private val bareRepo = {
    val d = new File(base, reponame +".git")
    if (!d.exists()) {
      val r = Git.init().setBare(true).setDirectory(d).call().getRepository
      log.info("Created bare repository at: "+ r.getDirectory)
      r
    } else {
      val r = new FileRepositoryBuilder()
        .setBare()
        .setGitDir(d)
        .readEnvironment()
        .build()
      log.info("Using bare repository at: "+ r.getDirectory)
      r
    }
  }

  Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
    def run() {
      close()
    }
  }))

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

      log.info("Created workspace at: "+ r.getWorkTree)
      r
    } else {
      val r = new FileRepositoryBuilder()
        .setWorkTree(root)
        .readEnvironment()
        .build();
      log.info("Using workspace at: "+ r.getWorkTree +" with git dir: "+ r.getDirectory)
      r
    }
  }


  private val git = new Git(workspaceRepo)

  def updateWorkspace():Boolean = {
    val result = git.pull().call()
    result.isSuccessful
  }

  private def push() {
    git.push()
      .setRemote("origin")
      .setRefSpecs(new RefSpec("master"))
      .call()
  }

  private def commit(c: GitFile, action:String) {
    git.commit()
      .setMessage(action +" on "+ c.name.fullName)
      .setAuthor("Publet Git", "no@none.com")
      .setAll(true)
      .call()
  }

  protected[git] def commitWrite(c: GitFile) {
    val path = Path(c.file).strip(c.rootPath)
    git.add()
      .addFilepattern(path.toRelative.asString)
      .setUpdate(false)
      .call()

    if (!git.status().call().isClean) {
      log.info("commit: "+ path.toRelative.asString)

      commit(c, "Update")
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
    log.info("Close git partition at: "+ workspaceRepo.getWorkTree)
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
