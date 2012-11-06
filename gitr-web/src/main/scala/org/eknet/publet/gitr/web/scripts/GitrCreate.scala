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
import org.eknet.publet.gitr.auth.{DefaultRepositoryStore, RepositoryModel, GitAction, RepositoryTag}
import org.eknet.publet.web.util.{PubletWeb, PubletWebContext}
import org.eknet.publet.gitr.GitRequestUtils
import org.eknet.gitr.{GitrMan, RepositoryName}

class GitrCreate extends ScalaScript {

  def checkName(repoName: String): Boolean = repoName.matches("[\\w_\\-]+")

  def serve() = {
    val tag = PubletWebContext.param("closed") match {
      case Some("on") => RepositoryTag.closed
      case _ => RepositoryTag.open
    }
    PubletWebContext.param("repositoryName") match {
      case Some(r) => {
        if (!checkName(r)) {
          makeJson(Map("success"->false, "message"->"Invalid repository name!"))
        } else {
          val login = Security.username
          val rootRepo = PubletWebContext.param("rootProject").collect({ case "on" => true}).getOrElse(false)
          val normName = if (rootRepo) r else login +"/"+ r
          if (rootRepo) {
            GitRequestUtils.checkGitAction(GitAction.createRoot, RepositoryModel(normName, RepositoryTag.open, login))
          }
          GitRequestUtils.checkGitAction(GitAction.create, RepositoryModel(normName, RepositoryTag.open, login))
          val repoName = RepositoryName(normName)
          PubletWeb.instance[GitrMan].get(repoName).map(x=> error("Repository already exists"))
            .getOrElse {
            val newRepo = PubletWeb.instance[GitrMan].create(repoName, true)
            PubletWebContext.param("description")
              .collect({case d if (!d.isEmpty) => d})
              .foreach(desc => newRepo.setDescription(desc))
            PubletWeb.instance[DefaultRepositoryStore].updateRepository(RepositoryModel(normName, tag, login))
            makeJson(Map(
              "success" -> true,
              "message" -> "Repository successfully created.",
              "giturl" -> PubletWebContext.urlOf("/git/"+ repoName.nameDotGit)
            ))
          }
        }
      }
      case _ => {
        error("No repository name given.")
      }
    }
  }

  def error(str:String) = makeJson(Map("success"->false, "message"->str))
}