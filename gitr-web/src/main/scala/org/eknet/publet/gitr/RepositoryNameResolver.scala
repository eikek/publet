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

import org.eknet.publet.web.util.{PubletWeb, Request, Key}
import org.eknet.publet.web.{Config, RequestAttr, RequestUrl}
import org.eknet.publet.gitr.auth.{DefaultRepositoryStore, GitAction}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 22:30
 */
trait RepositoryNameResolver {
  this: RequestUrl with RequestAttr =>

  private val gitRepositoryNameKey = Key("gitRepositoryName", {
    case Request => {
      PubletWeb.instance[RepositoryService].getRepositoryName(applicationPath, isGitRequest)
    }
  })

  /**
   * The name of the repository the current request points to.
   * @return
   */
  def getRepositoryName = attr(gitRepositoryNameKey).get

  private val repositoryModelKey = Key("requestRepositoryModel", {
    case Request => getRepositoryName.map { name =>
      PubletWeb.instance[DefaultRepositoryStore].getRepository(name)
    }
  })

  /**Returns the current repository and its state. If no state
   * is defined, the repository is considered [[org.eknet.publet.gitr.auth.RepositoryTag.open]]
   * If the current request does not point to an repository,
   * [[scala.None]] is returned.
   *
   * @return
   */
  def getRepositoryModel = attr(repositoryModelKey).get

  private val gitActionKey = Key("gitrequestAction", {
    case Request => {
      if (isGitRequest) {
        Some(PubletWeb.instance[RepositoryService].getGitRequestAction(fullUrl))
      } else {
        None
      }
    }
  })

  def isGitRequest = applicationUri.startsWith("/"+Config("publet.gitMount").getOrElse("git")+"/")

  /**Returns the action of the current request regarding the git
   * repository. It is either `pull` or `push` and only refers
   * to git clients. This only returns `Some` if `isGitRequest`
   * is `true`
   *
   * @return
   */
  def getGitAction: Option[GitAction.Action] = attr(gitActionKey).get

  private lazy val containerRequestUriKey = Key("containerRequestUri", {
    case Request => {
      PubletWeb.publet.mountManager.resolveMount(applicationPath)
        .map(t => applicationPath.strip(t._1)).get
    }
  })

  /**The part of this requests uri starting from the container
   * this request maps to. The path the container is mounted
   * at is stripped off.
   *
   * @return
   */
  def containerRequestUri = attr(containerRequestUriKey).get

}
