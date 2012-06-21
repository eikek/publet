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

import org.eknet.publet.gitr.GitrRepository
import org.eknet.publet.auth.{GitAction, RepositoryModel, RepositoryTag}
import org.eknet.publet.web.{PubletWeb, PubletWebContext}
import org.eknet.publet.web.shiro.Security
import org.eclipse.jgit.lib.Constants

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.05.12 20:24
 */
class RepositoryInfo(repo:GitrRepository, val model: RepositoryModel) extends Ordered[RepositoryInfo] {

  val name = repo.name
  val gitUrl = GitrControl.getCloneUrl(repo.name.name)
  val owner = model.owner
  val description = repo.getDescription.getOrElse("")
  private val lastCommitDate = {
    if (repo.hasCommits) {
      repo.getLastCommit(Constants.HEAD, None) match {
        case Some(c) => c.getCommitTime
        case _ => 0
      }
    } else {
      0
    }
  }

  lazy val toMap: Map[String, Any] = {
    Map(
      "name" -> (name.strip.segments.last),
      "fullName" -> name.name,
      "giturl" -> gitUrl,
      "owner" -> owner,
      "owned" -> (owner == Security.username),
      "tag" -> (model.tag.toString),
      "description" -> description,
      "push" -> (Security.hasGitAction(GitAction.push, model))
    )
  }

  def compare(that: RepositoryInfo) = that.lastCommitDate - this.lastCommitDate
}
