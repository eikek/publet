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
import org.eclipse.jgit.revwalk.RevCommit
import org.eknet.publet.web.{PubletWebContext, PubletWeb}
import ScalaScript._

// Returns a json array containing a tree of a repository
// Expects the following parameter
//   r = repository name
//   h  = ref spec (master, tag/bla, #34da3), defaults to 'master'
//   p = path inside the tree, defaults to '/'

class GitrView extends ScalaScript {

  val rParam = "r"
  val hParam = "h"
  val pParam = "p"


  def getCommitFromRequest(repo:GitrRepository): Option[RevCommit] = {
    val param = PubletWebContext.param(hParam).collect({ case e if (!e.isEmpty) => e}).getOrElse("master")
    repo.getCommit(param)
  }


  def serve() = {
    val cp = PubletWebContext.param(pParam).getOrElse("")
    val cpPath = {
      if (cp.isEmpty) Path.root
      else {
        Path(Path(cp).segments, false, false)
      }
    }

    PubletWebContext.param(rParam) flatMap (repoName => PubletWeb.gitr.get(RepositoryName(repoName))) match {
      case None => makeJson(Map("success"->false, "message"->"No repository found."))
      case Some(repo) => {
        getCommitFromRequest(repo) map { commit =>
          val treewalk = new TreeWalk(repo)
          treewalk.addTree(commit.getTree)
          if (!cp.isEmpty) treewalk.setFilter(PathFilter.create(cpPath.asString))
          val result= new ListBuffer[CommitInfo]()
          while (treewalk.next()) {
            if (cpPath.prefixedBy(Path(treewalk.getPathString))) {
              treewalk.enterSubtree()
            } else {
              val lastcommit = repo.getLastCommit(treewalk.getPathString).get
              result.append(new CommitInfo(treewalk, lastcommit))
            }
          }
          makeJson {
            Map(
              "success"->true,
              "files"-> result.sorted.map(_.toMap),
              "parent" -> !cpPath.isRoot,
              "parentPath"-> (if (cpPath.isRoot) "" else cpPath.parent.asString),
              "containerPath"-> Path(cpPath.segments, true, true).asString
            )
          }
        } getOrElse {
          makeJson(Map("success"->false, "message"->"Repository is empty."))
        }
      }
    }

  }
}
