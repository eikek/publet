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
import org.eknet.publet.web.{PubletWebContext, PubletWeb}
import ScalaScript._
import org.eknet.publet.auth.RepositoryTag
import org.eknet.publet.gitr.RepositoryName

class GitrRepoList extends ScalaScript {
  def serve() = {

    // by default returns users repositories
    // if param `all` is present, returns all open repositories
    // if param `closed` is present, returns only users closed repositories
    val all = PubletWebContext.param("all").isDefined
    val closed = PubletWebContext.param("closed").isDefined
    val name = PubletWebContext.param("name").getOrElse("")

    def getRepositoryTag(name: String) = {
      val n = if (name.endsWith(".git")) name.substring(0, name.length-4) else name
      PubletWeb.authManager.getRepository(n).tag
    }

    val gitr = PubletWeb.gitr
    if (all) {
      makeJson(PubletWeb.authManager.getAllRepositories
        .filter(_.tag == RepositoryTag.open)
        .flatMap(rm => gitr.get(RepositoryName(rm.name).toDotGit))
        .filter(_.name.segments.last.startsWith(name))
        .map(r => new RepositoryInfo(r, RepositoryTag.open).toMap))
    } else {
      val login = Security.username
      makeJson(gitr.allRepositories(_.name.startsWith(login +"/"))
        .filter(r => r.name.segments.last.startsWith(name))
        .map(r => (r, getRepositoryTag(r.name.name)))
        .filter(t => if (closed) t._2 == RepositoryTag.closed else true)
        .map(t => new RepositoryInfo(t._1, t._2).toMap))
    }
  }
}