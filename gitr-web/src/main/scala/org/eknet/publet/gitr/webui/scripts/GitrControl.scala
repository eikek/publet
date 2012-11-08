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

package org.eknet.publet.gitr.webui.scripts

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.util.RenderUtils._
import GitrControl._
import org.eknet.publet.com.twitter.json.Json
import org.eclipse.jgit.revwalk.RevCommit
import org.fusesource.scalate.TemplateEngine
import org.eknet.publet.web.util.{PubletWeb, PubletWebContext, RenderUtils}
import org.eknet.publet.vfs.{ContentType, Path, Content}
import scala.Some
import org.eknet.gitr.{GitrMan, GitrRepository, RepositoryName}
import org.eknet.publet.gitr.auth.{DefaultRepositoryStore, GitAction}
import org.eknet.publet.gitr.GitRequestUtils

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.05.12 19:25
 */
class GitrControl extends ScalaScript {

  def serve() = {
    getRepositoryModelFromParam match {
      case None => repositoryListing
      case Some(model) => getAction match {
        case "log" => {
          GitRequestUtils.checkGitAction(GitAction.pull, model)
          logView
        }
        case "commit" => {
          GitRequestUtils.checkGitAction(GitAction.pull, model)
          commitContents
        }
        case "admin" => {
          GitRequestUtils.checkGitAction(GitAction.edit, model)
          repositoryAdmin
        }
        case _ => {
          GitRequestUtils.checkGitAction(GitAction.pull, model)
          sourceView
        }
      }
    }
  }

  def renderEmptyRepoPage(): Option[Content] = {
    val repo = getRepositoryFromParam.map(r => {
      val last = r.name.segments.last
      last.substring(0, last.length-4) //without .git
    })
    renderTemplate(gitremptyRepoTemplate, repoHeadMap ++ Map("repoName"->(repo.getOrElse(""))))
  }

  def repoHeadMap: Map[String, Any] = {
    val repo = getRepositoryModelFromParam
    val currentHead = getRev
    Map(
      "repositoryModel" -> repo,
      "currentHead" -> currentHead
    )
  }

  def pageHeaderMap: Map[String, Any] = {
    val base = repoHeadMap
    val repo = getRepositoryFromParam
    val revisions = repo
      .map(r => r.getLocalBranches ::: r.getLocalTags)
      .map(_.map(_.name))
      .map(names => Json.build(names).toString)
      .getOrElse("")
    base ++ Map("repository"->repo, "revisions" -> revisions)
  }

  def repositoryAdmin: Option[Content] = {
    renderTemplate(gitrrepoAdminTemplate, repoHeadMap)
  }

  def repositoryListing: Option[Content] = {
    renderTemplate(gitradminTemplate, Map[String, Any]())
  }

  def sourceView: Option[Content] = {
    val repo = getRepositoryFromParam
    if (repo.isEmpty) {
      RenderUtils.renderMessage("No repository!", "Repository not found.", "error")
    } else {
      val c = getCommitFromRequest(repo.get)
      if (c.isEmpty) {
        renderEmptyRepoPage()
      } else {
        val params = pageHeaderMap ++ Map("path" -> (getPath.asString))
        renderTemplate(gitrsourceTemplate, params)
      }
    }
  }

  def logView: Option[Content] = {
    import collection.JavaConversions._

    getRepositoryFromParam flatMap ( repo => {
      repo.getCommit(getRev) flatMap ( commit => {
        val log = repo.git.log()
        val path = getPath
        if (!path.isRoot) {
          log.addPath(path.toRelative.asString)
        }
        log.add(commit.getId)
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
          Some("?r="+getReponame.get+ "&page="+(page+1)+"&do=log&h="+getRev)
        } else {
          None
        }
        val prevPage = if (page>1) {
          Some("?r="+getReponame.get+ "&page="+(page-1)+"&do=log&h="+getRev)
        } else {
          None
        }
        collectCommits(commitIter, 0)
        val commitInfos = commits.build
        val paramMap = pageHeaderMap ++ Map("path" -> (getPath.asString),
          "prevPage" -> prevPage,
          "nextPage" -> nextPage,
          "commits" -> commitInfos)
        renderTemplate(gitrlogTemplate, paramMap)

      }) orElse(renderEmptyRepoPage())
    }) orElse(RenderUtils.renderMessage("No repository!", "Repository not found.", "error"))
  }


  def commitContents = {
    getRepositoryFromParam map ( repo => {
      getCommitFromRequest(repo) flatMap ( commit => {
        val files = repo.getFilesInCommit(commit)
        val parent = repo.getParentCommit(commit).orNull
        val attrs = pageHeaderMap ++ Map(
          "path" -> (getPath.asString),
          "commit" -> commit,
          "changedFiles" -> files,
          "parent" -> parent)
        RenderUtils.renderTemplate(gitrcommitTemplate, attrs)
      })
    }) getOrElse {
      import ScalaScript._
      makeJson(Map("success"->false, "message"->"Repository not found."))
    }
  }
}

object GitrControl {

  lazy val mountPoint = PubletWeb.publetSettings("gitr.mountpoint").getOrElse("/gitr")

  /** Permission granted to administrate the repository (incl. delete) */
  val adminPerm = GitAction.edit.name

  /** Permission granted to allow creation of new repositories. */
  val createPerm = GitAction.create.name

  /** Permission granted to allow creation of new root repositories */
  val createRootPerm = GitAction.createRoot.name

  val gitradminTemplate = mountPoint+"/_gitradmin.page"
  val gitrrepoAdminTemplate = mountPoint+"/_repoadmin.page"
  val gitrsourceTemplate = mountPoint+"/_gitrbrowse.page"
  val gitrlogTemplate = mountPoint+"/_gitrlog.page"
  val gitrheaderTemplate = mountPoint+"/_gitrpagehead.page"
  val gitrcommitTemplate = mountPoint+"/_gitrcommit.page"
  val gitremptyRepoTemplate = mountPoint+"/_emptyrepo.page"

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

  def getRepositoryFromParam = PubletWebContext.param(rParam) flatMap (repoName => PubletWeb.instance[GitrMan].get.get(RepositoryName(repoName)))
  def getRepositoryModelFromParam = PubletWebContext.param(rParam).map(n=>PubletWeb.instance[DefaultRepositoryStore].get.getRepository(RepositoryName(n)))

  def getCommitFromRequest(repo:GitrRepository): Option[RevCommit] = {
    val param = getRev
    repo.getCommit(param)
  }

  lazy val nocachingTemplateEngine = {
    val templ = new TemplateEngine()
    templ.allowCaching = false
    templ
  }
  lazy val wikiExtensions = ContentType.markdown.extensions ++ ContentType.textile.extensions

  def getCloneUrl(repoName: String) = {
    val name = if (repoName.endsWith(".git")) repoName else repoName+".git"
    PubletWebContext.urlOf("/git/"+ name)
  }
}
