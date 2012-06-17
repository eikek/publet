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
import org.eknet.publet.web.shiro.Security
import ScalaScript._
import org.eknet.publet.auth.{RepositoryModel, GitAction, RepositoryTag}
import org.eknet.publet.gitr.RepositoryName
import org.eknet.publet.web.{PubletWebContext, PubletWeb}

class GitrRepoList extends ScalaScript {

  val authM = PubletWeb.authManager

  def getRepositoryModel(name: String) = authM.getRepository(name)

  /**
   * Returns only open repos. Used with filter="open"
   *
   * @param name
   * @param r
   * @return
   */
  private def filterOpen(name: String)(r: RepositoryName) : Boolean = {
    val model = getRepositoryModel(r.name)
    model.tag == RepositoryTag.open && r.segments.last.startsWith(name)
  }

  /**
   * Returns only owned repos. Used with filter="mine"
   *
   * @param name
   * @param r
   * @return
   */
  private def filterMine(name: String)(r:RepositoryName): Boolean = {
    val login = Security.username
    val model = getRepositoryModel(r.name)
    model.owner == login && r.segments.last.startsWith(name)
  }

  /**
   * Returns all collaborator repos. Used with filter="collab". These are
   * all repos, that are not owned but either given explicite pull permission
   * or push permission.
   *
   * @param name
   * @param r
   * @return
   */
  private def filterCollabs(name: String)(r: RepositoryName): Boolean = {
    val login = Security.username
    val model = getRepositoryModel(r.name)
    def hasPull = Security.hasGitAction(GitAction.pull, model)
    def hasPush = Security.hasGitAction(GitAction.push, model)

    // all repos with explicit permissions = all closed with pull rights and all with push rights (unowned)
    model.owner != login && r.segments.last.startsWith(name) &&
      (hasPull && model.tag == RepositoryTag.closed || hasPush)
  }


  private def getFilter(filter: String, name: String) = filter match {
    case "open" => filterOpen(name)_
    case "mine" => filterMine(name)_
    case "collab" => filterCollabs(name)_
    case _ => filterOpen(name)_
  }

  def serve() = {

    val filterName = PubletWebContext.param("filter").getOrElse("open")

    // the first part of the name of the repository
    val name = PubletWebContext.param("name").getOrElse("")

    def repoFilter = {
      if (Security.isAuthenticated) getFilter(filterName, name)
      else filterOpen(name)_
    }

    makeJson(PubletWeb.gitr.allRepositories(repoFilter)
      .map(r => (r, getRepositoryModel(r.name.name)))
      .map(t => new RepositoryInfo(t._1, t._2).toMap))
  }

}