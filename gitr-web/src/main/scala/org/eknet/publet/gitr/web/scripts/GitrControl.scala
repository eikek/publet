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

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.util.RenderUtils._
import GitrControl._
import org.eknet.publet.web.{PubletWeb, PubletWebContext}
import org.eknet.publet.com.twitter.json.Json
import org.eknet.publet.vfs.{Path, Content}
import org.eknet.publet.gitr.{GitrRepository, RepositoryName}
import org.eclipse.jgit.revwalk.RevCommit

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.05.12 19:25
 */
class GitrControl extends ScalaScript {
  def serve() = {
    getReponame match {
      case None => repositoryListing
      case Some(name) => getAction match {
        case "log" => logView
        case _ => sourceView
      }
    }
  }

  def repositoryListing: Option[Content] = {
    renderTemplate(gitradminTemplate, Map[String, Any]())
  }

  def sourceView: Option[Content] = {
    val repo = getRepositoryFromParam
    val revisions = repo
      .map(r => r.getLocalBranches ::: r.getLocalTags)
      .map(_.map(_.name))
      .map(names => Json.build(names).toString)
      .getOrElse("")
    val owner = repo map { r => if (r.name.segments.length > 1) r.name.segments(0) else "" } getOrElse ("")
    val model = repo.flatMap(r => PubletWeb.authManager.getRepository(r.name.name))
    val currentHead = getRev
    renderTemplate(gitrsourceTemplate, Map(
      "revisions" -> revisions,
      "repositoryModel" -> model,
      "owner" -> owner,
      "currentHead" -> currentHead,
      "path" -> (getPath.asString))
    )
  }

  def logView: Option[Content] = {
    import collection.JavaConversions._

    val repo = getRepositoryFromParam
    val revisions = repo
      .map(r => r.getLocalBranches ::: r.getLocalTags)
      .map(_.map(_.name))
      .map(names => Json.build(names).toString)
      .getOrElse("")
    val owner = repo map { r => if (r.name.segments.length > 1) r.name.segments(0) else "" } getOrElse ("")
    val model = repo.flatMap(r => PubletWeb.authManager.getRepository(r.name.name))
    val currentHead = getRev
    getRepositoryFromParam flatMap ( repo => {
      val log = repo.git.log()
      val path = getPath
      if (!path.isRoot) {
        log.addPath(path.toRelative.asString)
      }
      repo.getCommit(getRev).foreach(c => log.add(c.getId))

      val pageSize = PubletWeb.publetSettings("gitrweb.pageSize").getOrElse("25").toInt
      val page = getPage

      val commits = CommitGroup.newBuilder
      def collectCommits(iter:Iterator[RevCommit], cur: Int) {
        if (cur < (pageSize*page) && iter.hasNext) {
          val revCommit = iter.next()
          if (cur >= (pageSize*(page-1))) {
            commits.addByDay(CommitInfo("", "", false, revCommit, PubletWebContext.getLocale))
          }
          collectCommits(iter, cur+1)
        }
      }
      val commitIter = log.call().iterator()
      val nextPage = if (commitIter.hasNext) {
        Some("?r="+getReponame.get+ "&page="+(page+1)+"&do=log&rev="+getRev)
      } else {
        None
      }
      val prevPage = if (page>1) {
        Some("?r="+getReponame.get+ "&page="+(page-1)+"&do=log&rev="+getRev)
      } else {
        None
      }
      collectCommits(commitIter, 0)
      val commitInfos = commits.build
      renderTemplate(gitrlogTemplate, Map("revisions" -> revisions,
        "repositoryModel" -> model,
        "owner" -> owner,
        "currentHead" -> currentHead,
        "path" -> (getPath.asString),
        "prevPage" -> prevPage,
        "nextPage" -> nextPage,
        "commits" -> commitInfos))
    })
  }
}

object GitrControl {

  val gitradminTemplate = "/gitr/_gitradmin.page"
  val gitrsourceTemplate = "/gitr/_gitrbrowse.page"
  val gitrlogTemplate = "/gitr/_gitrlog.page"
  val gitrheaderTemplate = "/gitr/_gitrpagehead.page"

  val rParam = "r"   // repo name, default = "None"
  val hParam = "h"   // revision, default = "master"
  val pParam = "p"   // pathname, default = "/"
  val doParam = "do" // log|source, default = source
  val pageParam = "page" // the page to display

  def getPage = PubletWebContext.param(pageParam).getOrElse("1").toInt
  def getReponame = PubletWebContext.param(rParam)
  def getRev = PubletWebContext.param(hParam).collect({ case e if (!e.isEmpty) => e}).getOrElse("master")
  def getPath = {
    val cp = PubletWebContext.param(pParam).getOrElse("")
    if (cp.isEmpty) Path.root
    else {
      Path(Path(cp).segments, false, false)
    }
  }
  def getAction = PubletWebContext.param(doParam).getOrElse("source")

  def getRepositoryFromParam = PubletWebContext.param(rParam) flatMap (repoName => PubletWeb.gitr.get(RepositoryName(repoName)))
  def getRepositoryModelFromParam = getRepositoryFromParam.flatMap(r=>PubletWeb.authManager.getRepository(r.name.name))

  def getCommitFromRequest(repo:GitrRepository): Option[RevCommit] = {
    val param = getRev
    repo.getCommit(param)
  }
}
