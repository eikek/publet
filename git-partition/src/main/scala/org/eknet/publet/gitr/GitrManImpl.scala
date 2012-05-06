package org.eknet.publet.gitr

import scala.collection.mutable
import java.io.File
import org.eclipse.jgit.api.Git
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.05.12 12:32
 */
class GitrManImpl(root: File) extends GitrMan with Logging {

  private val daemonExportOk = "git-daemon-export-ok"
  private val names = mutable.Set[RepositoryName]()

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
    names add name
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
    names remove name
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

    names add target
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

  def allRepositories(f: (RepositoryName) => Boolean) = {
    names.filter(f).map(get(_).get)
  }

  def closeAll() {
    allRepositories(r=>true).foreach(_.close())
  }
}
