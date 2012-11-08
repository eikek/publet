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

package org.eknet.publet.gitr

import javax.servlet.http.HttpServletRequest
import org.eknet.publet.web.{Config, RequestAttr, RequestUrl}
import org.eknet.publet.gitr.auth.{GitPermissionBuilder, RepositoryModel, GitAction}
import org.eknet.publet.web.shiro.Security

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.11.12 20:07
 */
class GitRequestUtils(val req: HttpServletRequest) extends RepositoryNameResolver with RequestUrl with RequestAttr {

}

object GitRequestUtils extends GitPermissionBuilder {
  implicit def toGitRequestUtils(req: HttpServletRequest) = new GitRequestUtils(req)

  def gitMount(config: Config) = config("publet.gitMount").getOrElse("git")

  def checkGitAction(action: GitAction.Action, model: RepositoryModel) {
    Security.checkPerm(git action(action) on model.name)
  }
  def hasGitAction(action: GitAction.Action, model: RepositoryModel): Boolean = {
    Security.hasPerm(git action(action) on model.name)
  }
}
