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
 *
 * Many parts of the code are taken with much appreciation from the
 * class `com.gitblit.utils.JGitUtils` of the gitblit project. Gitblit
 * is licensed under the Apache License 2.0.
 *
 * Copyright 2011 gitblit.com.
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

package org.eknet.publet.gitr

import collection.JavaConversions._
import org.eclipse.jgit.api.Git
import java.io.File
import java.nio.file.Files
import io.Source
import org.eclipse.jgit.revwalk.{RevObject, RevWalk, RevCommit}
import org.eclipse.jgit.lib.{Constants, Ref, Repository}
import collection.mutable.ListBuffer

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


  def getCommit(id: String): Option[RevCommit] = {
    Option(self.resolve(id)) map { objId =>
      val walk = new RevWalk(self)
      val commit = walk.parseCommit(objId)
      walk.dispose()
      commit
    }
  }

  def getLastCommit(path: String): Option[RevCommit] = {
    val iter = git.log().addPath(path).call().iterator()
    if (iter.hasNext)
      Some(iter.next())
    else
      None
  }

  def hasCommits: Boolean = self.resolve(Constants.HEAD) != null

  /**
   * Get a list of refs in the repository.
   *
   * Adopted from gitblit.
   *
   * @param prefix the ref to get, like "refs/heads/", "refs/tags" etc, look at [[org.eclipse.jgit.lib.Constants]]
   * @return
   */
  def getRefs(prefix: String): List[RefModel] = {
    if (!hasCommits) List()
    else {
      val walk = new RevWalk(self)
      val buffer = ListBuffer[RefModel]()
      for (t <- self.getRefDatabase.getRefs(prefix)) {
        buffer.append(RefModel(t._1, t._2, walk.parseAny(t._2.getObjectId)))
      }
      walk.dispose()
      buffer.sorted.toList
    }
  }

  def getLocalBranches:List[RefModel] = getRefs(Constants.R_HEADS)

  override def toString = self.toString
}

case class RefModel(name: String, ref: Ref, obj: RevObject) extends Ordered[RefModel] {
  def compare(that: RefModel) = name.compare(that.name)
}

object GitrRepository {

  def apply(repo: Repository, name: RepositoryName) = new GitrRepository(repo, name)
  implicit def repoToGitrRepo(repo: Repository, name: RepositoryName): GitrRepository = GitrRepository(repo, name)
  implicit def gitrRepoToRepo(grepo: GitrRepository): Repository = grepo.self

}
