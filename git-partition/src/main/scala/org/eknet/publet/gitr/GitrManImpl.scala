package org.eknet.publet.gitr

import org.eclipse.jgit.api.Git
import grizzled.slf4j.Logging
import org.eclipse.jgit.util.FS
import java.io.{FileFilter, File}
import org.eclipse.jgit.lib.RepositoryCache

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.05.12 12:32
 */
class GitrManImpl(root: File) extends GitrMan with Logging {

  private val daemonExportOk = "git-daemon-export-ok"

  if (!root.exists()) {
    if (!root.mkdirs()) sys.error("Cannot create directory for git repos")
  }
  if (!root.isDirectory) sys.error("Not a directory: "+ root)

  private def repoFile(name: RepositoryName) = new File(root, name.path.segments.mkString(File.separator))

  def exists(name: RepositoryName) = repoFile(name).exists()

  def get(name: RepositoryName) = {
    val file = repoFile(name)
    if (!file.exists()) None
    else Some(Git.open(file).getRepository)
  }

  def create(name: RepositoryName, bare: Boolean) = {
    val file = repoFile(name)
    if (file.exists()) sys.error("Repository '"+name+"' already exists")
    val repo = Git.init().setBare(bare).setDirectory(file).call().getRepository
    repo
  }

  def getOrCreate(name: RepositoryName, bare: Boolean) = {
    get(name) getOrElse {
      create(name, bare)
    }
  }

  def delete(name: RepositoryName) {
    val file = repoFile(name)
    if (!file.exists()) sys.error("Repository '"+name+"' does not exist")

    def deleteall(f: File) {
      if (f.isDirectory && f.exists()) f.listFiles().foreach(deleteall)
      f.delete()
    }
    deleteall(file)
  }

  def clone(source: RepositoryName, target: RepositoryName, bare: Boolean) = {
    val repo = get(source).get
    val file = repoFile(target)
    if (file.exists()) sys.error("Repository '"+target+"' already exists")
    val cloned = Git.cloneRepository()
      .setBare(bare)
      .setCloneAllBranches(true)
      .setURI(repo.getDirectory.toURI.toString)
      .setDirectory(file)
      .call().getRepository

    cloned
  }

  private def getExportOkFile(name: RepositoryName) = {
    val repo = get(name).get
    new File(repo.getDirectory, daemonExportOk)
  }

  def isExportOk(name: RepositoryName) = {
    val exportok = getExportOkFile(name)
    exportok.exists()
  }

  def setExportOk(name: RepositoryName, flag: Boolean) = {
    val exportok = getExportOkFile(name)
    val prev = exportok.exists()
    if (!flag) exportok.delete()
    else if (!exportok.exists()) {
      exportok.createNewFile()
    }
    prev
  }

  def getGitrRoot = root

  private def isRepo(f: File) = Option(RepositoryCache.FileKey.resolve(f, FS.detect()))

  private val dfilter = new FileFilter {
    def accept(pathname: File) = pathname.getName != ".git" && pathname.isDirectory
  }

  def allRepositories(f: (RepositoryName) => Boolean) = {
    def children(xf: File) = if (xf.isDirectory) xf.listFiles(dfilter) else Array[File]()
    def tree(xf: File):Seq[File] = Seq(xf) ++ children(xf).flatMap(c => tree(c))
    tree(root).filter(isRepo(_).isDefined)
      .filter(dir => f(RepositoryName(dir.getAbsolutePath.substring(root.getAbsolutePath.length+1))))
      .map(dir=>Git.open(dir).getRepository)
  }

  def closeAll() {
    allRepositories(r=>true).foreach { r =>
      info("Closing repository: "+ r)
      r.close()
    }
  }
}
