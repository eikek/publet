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
import org.eknet.publet.vfs.Content
import org.eknet.publet.web.{PubletWeb, PubletWebContext}
import org.eknet.publet.gitr.RepositoryName
import org.eknet.publet.com.twitter.json.Json

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.05.12 19:25
 */
class GitrControl extends ScalaScript {
  def serve() = {
    getReponame match {
      case None => repositoryListing
      case Some(name) => {
        if (getAction == "log") {
          logView
        } else {
          sourceView
        }
      }
    }
  }

  def repositoryListing: Option[Content] = {
    renderTemplate(gitradminTemplate, Map[String, Any]())
  }

  def sourceView: Option[Content] = {
    val repo = getRepositoryFromParam
    val revisions = getRepositoryFromParam
      .map(_.getLocalBranches)
      .map(_.map(_.name))
      .map(names => Json.build(names).toString)
      .getOrElse("")
    val owner = repo map { r => if (r.name.segments.length > 1) r.name.segments(0) else "" } getOrElse ("")
    val model = repo.flatMap(r => PubletWeb.authManager.getRepository(r.name.name))
    val currentHead = repo flatMap { r => Option(r.getBranch) } getOrElse ("")
    renderTemplate(gitrsourceTemplate, Map(
      "revisions" -> revisions,
      "repositoryModel" -> model,
      "owner" -> owner,
      "currentHead" -> currentHead,
      "path" -> getPath)
    )
  }

  def logView: Option[Content] = {
    sys.error("not implemented")
  }
}
object GitrControl {

  val gitradminTemplate = "/gitr/_gitradmin.page"
  val gitrsourceTemplate = "/gitr/_gitrbrowse.page"

  val rParam = "r"   // repo name, default = "None"
  val hParam = "h"   // revision, default = "master"
  val pParam = "p"   // pathname, default = "/"
  val doParam = "do" // log|source, default = source

  def getReponame = PubletWebContext.param(rParam)
  def getRev = PubletWebContext.param(hParam).getOrElse("master")
  def getPath = PubletWebContext.param(pParam).getOrElse("/")
  def getAction = PubletWebContext.param(doParam).getOrElse("source")

  def getRepositoryFromParam = PubletWebContext.param(rParam) flatMap (repoName => PubletWeb.gitr.get(RepositoryName(repoName)))
  def getRepositoryModelFromParam = getRepositoryFromParam.flatMap(r=>PubletWeb.authManager.getRepository(r.name.name))
}
