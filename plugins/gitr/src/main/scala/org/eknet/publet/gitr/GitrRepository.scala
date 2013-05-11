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
import java.nio.charset.Charset
import org.eclipse.jgit.lib._
import org.eclipse.jgit.revwalk._
import java.util.Date
import org.eclipse.jgit.treewalk.filter.{AndTreeFilter, TreeFilter, PathFilter}
import org.eclipse.jgit.diff.{DiffFormatter, RawTextComparator}
import java.io.{ByteArrayOutputStream, InputStream, File}
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import GitrRepository._
import org.eclipse.jgit.util.io.DisabledOutputStream

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 23:43
 */
class GitrRepository(val self: Repository, val name: RepoName) {
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

  /**
   * Creates a diff between two commits.
   *
   * @param base
   * @param commit
   * @param path
   */
  def getDiff(base: Option[RevCommit], commit: RevCommit, path: Option[String]) = {
    val baos = new ByteArrayOutputStream()
    val df = new DiffFormatter(baos)
    formatDiff(commit, df, base, path)
    val diff = baos.toString
    df.flush()
    diff
  }

  /**
   * Formats a diff between two commits using the supplied [[org.eclipse.jgit.diff.DiffFormatter]]
   *
   * This method was originally found at gitblit project (http://gitblit.com/) in `DiffUtils`
   * and changed to scala code.
   *
   * @param commit
   * @param formatter
   * @param base
   * @param path
   */
  def formatDiff(commit: RevCommit, formatter: DiffFormatter, base: Option[RevCommit], path: Option[String]) {
    val cmp = RawTextComparator.DEFAULT
    formatter.setRepository(self)
    formatter.setDiffComparator(cmp)
    formatter.setDetectRenames(true)

    val commitTree = commit.getTree
    val baseTree = base.map(_.getTree).getOrElse {
      if (commit.getParentCount > 0) {
        val rw = new RevWalk(self)
        val par = rw.parseCommit(commit.getParent(0).getId)
        rw.dispose()
        par.getTree
      } else {
        commitTree
      }
    }

    val diffEntries = formatter.scan(baseTree, commitTree)
    path.collect({case s if (!s.isEmpty)=>s}) match {
      case Some(p) => diffEntries.find(_.getNewPath == p).map(formatter.format(_))
      case _ => formatter.format(diffEntries)
    }
  }

  /**
   * Returns the lines of the specified source file annotated with
   * the author information.
   *
   * This method was originally found in the gitblit project (http://gitblit.com)
   * and formatted to scala code.
   *
   * @param path
   * @param objectId
   * @return
   */
  def getBlame(path: String, objectId: String): Seq[AnnotatedLine] = {
    val result = git.blame()
      .setFilePath(path)
      .setStartCommit(self.resolve(objectId))
      .call()

    val rawText = result.getResultContents
    for (i <- 0 to rawText.size()-1) yield {
      new AnnotatedLine(result.getSourceCommit(i), i+1, rawText.toString)
    }
  }

  /**
   * Returns a list of files that changed in the given commit.
   *
   * This method was found at the gitblit project (http://gitblit.com) in
   * `JGitUtils` class.
   *
   * @param commit
   * @return
   */
  def getFilesInCommit(commit: RevCommit): List[PathModel] = {
    if (!hasCommits) {
      List()
    } else {
      if (commit.getParentCount == 0) {
        val tw = new TreeWalk(self)
        tw.reset()
        tw.setRecursive(true)
        tw.addTree(commit.getTree)
        val models = withTreeWalk(tw) { t =>
          PathModel(t.getPathString, t.getPathString, 0, t.getRawMode(0), commit.getId.getName, Some(ChangeType.ADD))
        }
        tw.release()
        models
      } else {
        val parent = getParentCommit(commit).get
        val df = new DiffFormatter(DisabledOutputStream.INSTANCE)
        df.setRepository(self)
        df.setDiffComparator(RawTextComparator.DEFAULT)
        df.setDetectRenames(true)
        val entries = df.scan(parent.getTree, commit.getTree)
        val models = for (entry <- entries) yield {
          entry.getChangeType match {
            case ChangeType.DELETE => PathModel(entry.getOldPath,
              entry.getOldPath, 0, entry.getNewMode.getBits,
              commit.getId.getName, Some(entry.getChangeType))
            case ChangeType.RENAME => PathModel(entry.getOldPath,
              entry.getNewPath, 0, entry.getNewMode.getBits,
              commit.getId.getName, Some(entry.getChangeType))
            case _ => PathModel(entry.getNewPath,
              entry.getNewPath, 0, entry.getNewMode.getBits,
              commit.getId.getName, Some(entry.getChangeType))
          }
        }
        models.toList
      }
    }
  }

  def getParentCommit(commit: RevCommit): Option[RevCommit] = {
    if (commit.getParentCount > 0) {
      withRevWalk { rw =>
        Some(rw.parseCommit(commit.getParent(0).getId))
      }
    } else {
      None
    }
  }

  override def toString = self.toString

  def withRevWalk[A](f: RevWalk => A) = {
    val rw = new RevWalk(self)
    try {
      f(rw)
    } finally {
      rw.dispose()
    }
  }
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

case class AnnotatedLine(commitId: String, author: String, when: Date, line: Int, data: String) {
  def this(commit: RevCommit, line: Int, data: String) = this(commit.getName,
    commit.getAuthorIdent.getName, commit.getAuthorIdent.getWhen, line, data)
}

case class PathModel(name: String,
                     path: String,
                     size: Long,
                     mode: Int,
                     commitId: String,
                     changeType: Option[ChangeType] = None)


object GitrRepository {

  def apply(repo: Repository, name: RepoName) = new GitrRepository(repo, name)
  implicit def repoToGitrRepo(repo: Repository, name: RepoName): GitrRepository = GitrRepository(repo, name)
  implicit def gitrRepoToRepo(grepo: GitrRepository): Repository = grepo.self

  def withTreeWalk[A](tw: TreeWalk)(f: TreeWalk=>A): List[A] = {
    if (tw.next()) f(tw) :: withTreeWalk(tw)(f)
    else Nil
  }
}