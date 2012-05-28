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
 */

package org.eknet.publet.gitr.web.scripts

import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathFilter
import collection.mutable.ListBuffer
import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.gitr.{RepositoryName, GitrRepository}
import org.eknet.publet.vfs.Path
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}
import org.eknet.publet.web.{PubletWebContext, PubletWeb}
import ScalaScript._

// Returns a json array containing a tree of a repository
// Expects the following parameter
//   repo = repository name
//   ref  = ref spec (master, tag/bla, #34da3), defaults to 'master'
//   path = path inside the tree, defaults to '/'

class GitrView extends ScalaScript {

  def getCommit(repo:GitrRepository, id: String): RevCommit = {
    //todo test if repo is empty!
    val objId = repo.resolve(id)
    val walk = new RevWalk(repo)
    val commit = walk.parseCommit(objId)
    walk.dispose()
    commit
  }

  def getCommitFromRequest(repo:GitrRepository): RevCommit = {
    val param = PubletWebContext.param("ref").collect({ case e if (!e.isEmpty) => e}).getOrElse("master")
    getCommit(repo, param)
  }


  def serve() = {
    val cp = PubletWebContext.param("path").getOrElse("").replaceAll("/+", "/")
    val cpPath = {
      if (cp.isEmpty) Path.root
      else {
        Path(Path(cp).segments, false, false)
      }
    }

    PubletWebContext.param("repo") flatMap (repoName => PubletWeb.gitr.get(RepositoryName(repoName))) match {
      case None => makeJson(Map("success"->false, "message"->"No repository found."))
      case Some(repo) => {
        val commit = getCommitFromRequest(repo)
        val treewalk = new TreeWalk(repo)
        treewalk.addTree(commit.getTree)
        if (!cp.isEmpty) treewalk.setFilter(PathFilter.create(cpPath.asString))
        val result= new ListBuffer[CommitInfo]()
        while (treewalk.next()) {
          if (cpPath.prefixedBy(Path(treewalk.getPathString))) {
            treewalk.enterSubtree()
          } else {
            result.append(new CommitInfo(treewalk, commit))
          }
        }
        makeJson {
          Map(
            "success"->true,
            "files"-> result.sorted.map(_.toMap),
            "parent" -> !cpPath.isRoot,
            "parentPath"-> (if (cpPath.isRoot) "" else cpPath.parent.asString),
            "containerPath"-> cpPath.asString
          )
        }
      }
    }

  }
}

class CommitInfo(tree: TreeWalk, commit: RevCommit) extends Ordered[CommitInfo] {
  val name = tree.getNameString
  val path = tree.getPathString
  val container = tree.isSubtree
  val author = commit.getAuthorIdent.getName
  val message = commit.getFullMessage.split("\n")(0)
  val age = (System.currentTimeMillis() / 1000 - commit.getCommitTime)

  def toMap: Map[String, Any] = Map(
    "name" -> name,
    "container" -> container,
    "author" -> author,
    "message" -> message,
    "age" -> age,
    "icon" -> (if (container) "icon-folder-close" else "icon-file")
  )

  def compare(that: CommitInfo) = {
    if ((container && that.container) || (!container && !that.container)) {
      name.compare(that.name)
    } else {
      if (container) -1
      else +1
    }
  }
  override def toString = "CommitInfo["+name+":"+container+"]"
}
