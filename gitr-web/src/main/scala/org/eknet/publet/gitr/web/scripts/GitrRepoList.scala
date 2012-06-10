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
import org.eknet.publet.auth.RepositoryTag
import org.eknet.publet.gitr.{GitrRepository, RepositoryName}
import org.eknet.publet.web.{GitAction, PubletWebContext, PubletWeb}

class GitrRepoList extends ScalaScript {
  def serve() = {

    // by default returns all open repositories
    // that are repostories either defined as public
    // in permission database or are not defined at all

    // if 'mine' is present, only current user's repositories are returned
    val mine = PubletWebContext.param("mine").isDefined

    // if param `closed` is present, returns only users closed repositories
    val closed = PubletWebContext.param("closed").isDefined

    // the first part of the name of the repository
    val name = PubletWebContext.param("name").getOrElse("")
    val login = Security.username

    val authM = PubletWeb.authManager
    def getRepositoryTag(name: String) = {
      authM.getRepository(name).tag
    }

    def repoFilter = (r:RepositoryName) => {
      val tag = getRepositoryTag(r.name)
      if (!Security.isAuthenticated || !(mine || closed)) {
        tag == RepositoryTag.open && r.segments.last.startsWith(name)
      } else {
        val prefix = if (mine || closed) login+"/"+name else name
        if (closed) {
          tag == RepositoryTag.closed && r.name.startsWith(prefix)
        } else {
          //all repos with read access
          (tag == RepositoryTag.open ||
            Security.hasGitAction(GitAction.pull, authM.getRepository(r.name))) && r.name.startsWith(prefix)
        }
      }
    }

    makeJson(PubletWeb.gitr.allRepositories(repoFilter)
      .map(r => (r, getRepositoryTag(r.name.name)))
      .map(t => new RepositoryInfo(t._1, t._2).toMap))
  }

}