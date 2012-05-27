package org.eknet.publet.gitr

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.api.Git
import java.io.File
import java.nio.file.Files
import io.Source

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 23:43
 */
class GitrRepository(val self: Repository, val name: RepositoryName) {
  private val daemonExportOk = "git-daemon-export-ok"
  val git = Git.wrap(self)

  def addHttpReceivePack() {
    val cfg = self.getConfig
    cfg.setBoolean("http", null, "receivepack", true)
  }

  def isHttpReceivePack = {
    val cfg = self.getConfig
    cfg.getBoolean("http", null, "receivepack", false)
  }

  def isTandem = {
    val cfg = self.getConfig
    cfg.getBoolean("gitr", null, "tandem", false)
  }

  /**
   * Sets `str` in the description file. Only working
   * for bare repositories!
   *
   * @param str
   */
  def setDescription(str: String) {
    if (self.isBare) {
      val descFile = new File(self.getDirectory, "description")
      Files.write(descFile.toPath, str.getBytes)
    } else {
      sys.error("Not a bare repository! Cannot set description")
    }
  }

  /**
   * If this is a bare repository, looks for a `description`
   * file and returns its contents. An exception is thrown
   * if this is not a bare repository and [[scala.None]] is
   * returned, if the file does not exist.
   *
   * @return
   */
  def getDescription: Option[String] = {
    if (self.isBare) {
      val descFile = new File(self.getDirectory, "description")
      if (descFile.exists()) Some(Source.fromFile(descFile).mkString)
      else None
    } else {
      sys.error("Not a bare repository! Cannot set description")
    }
  }

  /**
   * Checks whether this repository contains the `git-export-ok`
   * file that allows access to the repo via http/s.
   *
   * @return
   */
  def isExportOk = getExportOkFile.exists()

  private def getExportOkFile = {
    if (!self.isBare) sys.error("git-daemon-export-ok files only for bare repos")
    new File(self.getDirectory, daemonExportOk)
  }

  /**
   * Sets the `git-export-ok` file or removes it as indicated
   * by the `flag` argument. Returns the previous state.
   *
   * @param flag
   * @return
   */
  def setExportOk(flag: Boolean) = {
    val exportok = getExportOkFile
    val prev = exportok.exists()
    if (!flag) exportok.delete()
    else if (!exportok.exists()) {
      exportok.createNewFile()
    }
    prev
  }



  override def toString = self.toString
}

object GitrRepository {

  def apply(repo: Repository, name: RepositoryName) = new GitrRepository(repo, name)
  implicit def repoToGitrRepo(repo: Repository, name: RepositoryName): GitrRepository = GitrRepository(repo, name)
  implicit def gitrRepoToRepo(grepo: GitrRepository): Repository = grepo.self

}
