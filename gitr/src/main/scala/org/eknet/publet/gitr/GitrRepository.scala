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
import java.nio.file.Files
import io.Source
import collection.mutable.ListBuffer
import org.eclipse.jgit.treewalk.TreeWalk
import java.io.{OutputStream, InputStream, File}
import java.nio.charset.Charset
import org.eclipse.jgit.lib._
import org.eclipse.jgit.revwalk._
import java.util.Date
import org.eclipse.jgit.treewalk.filter.{AndTreeFilter, TreeFilter, PathFilter}

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
      sys.error("'"+ name.name+ "' is not a bare repository! Cannot get description")
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

  def getLastCommit(branch: String, path: Option[String]): Option[RevCommit] = {
    Option(self.resolve(branch)) map { objId =>
      val walk = new RevWalk(self)
      path.collect({case p if (!p.isEmpty && p != "/") => p })
        .foreach(p => walk.setTreeFilter(AndTreeFilter.create(TreeFilter.ANY_DIFF, PathFilter.create(p))))
      walk.sort(RevSort.COMMIT_TIME_DESC)
      val head = walk.parseCommit(objId)
      walk.markStart(head)
      val commit = walk.next()
      walk.dispose()
      commit
    }
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
  def getLocalTags:List[RefModel] = getRefs(Constants.R_TAGS)

  /**
   * Gets the byte contents of a file in the tree.
   *
   * This method is taken from gitblit projects JGitUtils class.
   *
   * @param tree
   * @param path
   * @return
   */
  def getObject(tree: RevTree, path: String): Option[InputStream] = {
    getBlobLoader(tree, path) map { loader => loader.openStream() }
  }

  def getBlobLoader(tree: RevTree, path: String): Option[ObjectLoader] = {
    val rw = new RevWalk(self)

    def readBlob(walk: TreeWalk): Option[ObjectLoader] = {
      if (walk.isSubtree && path != walk.getPathString) {
        walk.enterSubtree()
        if (walk.next()) readBlob(walk)
        else None
      } else {
        val objid = walk.getObjectId(0)
        val objmode = walk.getFileMode(0)
        val ro = rw.lookupAny(objid, objmode.getObjectType)
        rw.parseBody(ro)
        Some(self.open(ro.getId, Constants.OBJ_BLOB))
      }
    }
    val tw = new TreeWalk(self)
    tw.setFilter(PathFilter.create(path))
    tw.reset(tree)
    if (tw.next()) readBlob(tw)
    else None
  }

  def getStringContents(tree: RevTree, path: String): Option[String] = {
    getBlobLoader(tree, path) map { c =>
      new String(c.getCachedBytes, Charset.forName(Constants.CHARACTER_ENCODING))
    }
  }

  def getDefaultBranch: Option[ObjectId] = {
    Option(self.resolve(Constants.HEAD)) match {
      case Some(h) => Some(h)
      case None => {
        getLocalBranches
          .sortWith((rf1, rf2) => rf1.getDate.after(rf2.getDate))
          .headOption.map(_.obj)
      }
    }
  }

  override def toString = self.toString
}

case class RefModel(name: String, ref: Ref, obj: RevObject) extends Ordered[RefModel] {
  def compare(that: RefModel) = name.compare(that.name)
  def getDate: Date = {
    val dateOpt = obj match {
      case c:RevCommit => Option(c.getCommitterIdent).map(_.getWhen)
      case t:RevTag => Option(t.getTaggerIdent).map(_.getWhen)
      case _ => None
    }
    dateOpt.getOrElse(new Date(0))
  }
}

object GitrRepository {

  def apply(repo: Repository, name: RepositoryName) = new GitrRepository(repo, name)
  implicit def repoToGitrRepo(repo: Repository, name: RepositoryName): GitrRepository = GitrRepository(repo, name)
  implicit def gitrRepoToRepo(grepo: GitrRepository): Repository = grepo.self

  private def copy(in: InputStream, out: OutputStream, closeOut: Boolean = true, closeIn: Boolean = true) {
    val buff = new Array[Byte](2048)
    var len = 0
    try {
      while (len != -1) {
        len = in.read(buff)
        if (len != -1) {
          out.write(buff, 0, len)
        }
      }
      out.flush();
    } finally {
      if (closeOut) out.close()
      if (closeIn) in.close()
    }
  }
}
