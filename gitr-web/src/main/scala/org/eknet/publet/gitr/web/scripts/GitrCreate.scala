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
import org.eknet.publet.web.{PubletWeb, PubletWebContext, GitAction}
import ScalaScript._
import org.eknet.publet.gitr.RepositoryName
import org.eknet.publet.auth.{RepositoryModel, RepositoryTag}
import org.eknet.publet.auth.xml.PermissionModel

class GitrCreate extends ScalaScript {

  def serve() = {
    val tag = PubletWebContext.param("closed") match {
      case Some("on") => RepositoryTag.closed
      case _ => RepositoryTag.open
    }
    PubletWebContext.param("repositoryName") match {

      case Some(r) => {
        val login = Security.username
        val normName = login +"/"+ r
        val repoName = RepositoryName(normName).toDotGit
        PubletWeb.gitr.get(repoName).map(x=> json(false, "Repository already exists"))
          .getOrElse {
          val newRepo = PubletWeb.gitr.create(repoName, true)
          PubletWebContext.param("description")
            .collect({case d if (!d.isEmpty) => d})
            .foreach(desc => newRepo.setDescription(desc))
          PubletWeb.authManager.updateRepository(RepositoryModel(normName, tag))
          PubletWeb.authManager.updatePermission(PermissionModel(GitAction.push.toString, List(normName), List(login)))
          PubletWeb.authManager.reload()
          makeJson(Map(
            "success" -> true,
            "message" -> "Repository successfully created.",
            "giturl" -> PubletWebContext.urlOf("/git/"+ repoName.name)
          ))
        }
      }
      case _ => {
        json(false, "No repository name given.")
      }
    }
  }

  def json(success:Boolean, str:String) = makeJson(Map("success"->success, "message"->str))
}