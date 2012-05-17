package org.eknet.publet.gitr

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.api.Git
import java.io.File

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
