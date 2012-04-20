package org.eknet.publet.partition.git

import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.RefSpec
import org.eknet.publet.Path
import java.io.{OutputStream, InputStream, File}
import org.eknet.publet.resource._
import scala.Option
import org.slf4j.LoggerFactory

/**
 *
 * @param base
 * @param reponame
 */
class GitPartition(id: Symbol, val base: File, reponame: String) extends FilesystemPartition(new File(base, reponame+"_wc"), id, false) {
  private val log = LoggerFactory.getLogger(getClass)

  def this(id: Symbol, base: String, reponame: String) = this(id, new File(base), reponame)

  // the working copy is checked out to $base/$reponame_wc while the bare repo is at $base/$reponame.git

  val bareRepo = {
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


  val workspaceRepo = {
    if (!root.exists()) {
      val r = Git.cloneRepository()
        .setBare(false)
        .setCloneAllBranches(true)
        .setURI(bareRepo.getDirectory.toURI.toString)
        .setDirectory(root)
        .call().getRepository

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

  private def push() {
    git.push()
      .setRemote("origin")
      .setRefSpecs(new RefSpec("master"))
      .call()
  }

  private def commit(c: ContentResource, action:String) {
    git.commit()
      .setMessage(action +" on "+ c.path.asString)
      .setAuthor("Publet Git", "no@none.com")
      .setAll(true)
      .call()
  }

  def commitWrite(c: ContentResource) {
    git.add()
      .addFilepattern(c.path.toRelative.asString)
      .setUpdate(false)
      .call()

    if (!git.status().call().isClean) {
      log.info("commit: "+ c.path.toRelative.asString)

      commit(c, "Update")
      push()
    }
  }

  def commitDelete(c: ContentResource) {
    git.rm().addFilepattern(c.path.toRelative.asString)
      .call()

    commit(c, "Delete")
    push()
  }

  override protected def newDirectory(f: File, root: Path) = GitPartition.newDirectory(f, root, this)
  override protected def newFile(f: File, root: Path) = GitPartition.newFile(f, root, this)

}

object GitPartition {

  def newDirectory(f: File, root: Path, gp: GitPartition): ContainerResource = new GitDirectory(f, root, gp)
  def newFile(f: File, root: Path, gp: GitPartition): ContentResource = new GitFile(f, root, gp)

}

class GitDirectory(dir:File, root:Path, gp: GitPartition) extends DirectoryResource(dir, root) {
  override protected def newDirectory(f: File, root: Path) = GitPartition.newDirectory(f, root, gp)
  override protected def newFile(f: File, root: Path) = GitPartition.newFile(f, root, gp)

  override def children:Iterable[_ <: Resource] = super.children.filterNot(_.path.asString.startsWith("/.git"))
}

class GitFile(f: File, root: Path, gp: GitPartition) extends FileResource(f, root) {
  override def delete() {
    super.delete()
    gp.commitDelete(this)
  }

  override def writeFrom(in: InputStream) {
    super.writeFrom(in)
  }

  override def outputStream:Option[OutputStream] = {
    val out = super.outputStream.get
    Some(new OutputStream {

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
        gp.commitWrite(GitFile.this)
      }

      override def flush() {
        out.flush()
      }
    })
  }

  override protected def newDirectory(f: File, root: Path) = GitPartition.newDirectory(f, root, gp)
  override protected def newFile(f: File, root: Path) = GitPartition.newFile(f, root, gp)

}