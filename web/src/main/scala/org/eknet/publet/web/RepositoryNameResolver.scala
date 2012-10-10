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

package org.eknet.publet.web

import util.{Request, Key}
import org.eclipse.jgit.http.server.GitSmartHttpTools
import org.eknet.publet.partition.git.GitPartition
import org.eknet.publet.gitr.RepositoryName
import org.eknet.publet.vfs.Path
import org.eknet.publet.auth.GitAction

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 22:30
 */
trait RepositoryNameResolver {
  this: RequestUrl with RequestAttr =>

  private val gitRepositoryNameKey = Key("gitRepositoryName", {
    case Request => {
      RepositoryNameResolver.getRepositoryName(applicationPath, isGitRequest)
    }
  })

  /**
   * The name of the repository the current request points to.
   * @return
   */
  def getRepositoryName = attr(gitRepositoryNameKey).get

  private val repositoryModelKey = Key("requestRepositoryModel", {
    case Request => getRepositoryName.map { name =>
      PubletWeb.authManager.getRepository(name.name)
    }
  })

  /**Returns the current repository and its state. If no state
   * is defined, the repository is considered [[org.eknet.publet.auth.RepositoryTag.open]]
   * If the current request does not point to an repository,
   * [[scala.None]] is returned.
   *
   * @return
   */
  def getRepositoryModel = attr(repositoryModelKey).get

  private val gitActionKey = Key("gitrequestAction", {
    case Request => {
      if (isGitRequest) {
        Some(RepositoryNameResolver.getGitRequestAction(fullUrl))
      } else {
        None
      }
    }
  })

  /**Returns the action of the current request regarding the git
   * repository. It is either `pull` or `push` and only refers
   * to git clients. This only returns `Some` if `isGitRequest`
   * is `true`
   *
   * @return
   */
  def getGitAction: Option[GitAction.Value] = attr(gitActionKey).get

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

object RepositoryNameResolver {

  private val gitReceivePack = "/" + GitSmartHttpTools.RECEIVE_PACK
  private val gitUploadPack = "/" + GitSmartHttpTools.UPLOAD_PACK
  private val gitSuffixes = List(gitReceivePack, gitUploadPack, "/info/refs", "/HEAD", "/objects")

  /**
   * Returns either `GitAction.pull` or `GitAction.push` depending on the request uri. This
   * is only valid for request pointing to the git repository managed by JGit's filter.
   *
   * @param fullUrl an url string with request parameters
   * @return
   */
  def getGitRequestAction(fullUrl: String): GitAction.Value = {
    val gitp = fullUrl.substring(Config.get.gitMount.length)
    if (gitp.endsWith(gitReceivePack))
      GitAction.push
    else if (gitp.endsWith(gitUploadPack))
      GitAction.pull
    else if (gitp.contains("?service=git-receive-pack"))
      GitAction.push
    else if (gitp.contains("?service=git-upload-pack"))
      GitAction.pull
    else
      GitAction.pull
  }

  /**
   * Returns the repository name of the resource specified by `applicationPath`.
   * If `isGitRequest` is `true`, the `applicationPath` is considered a request
   * path to the git servlet and the repository name is encoded in the url.
   *
   * Otherwise, the repository is determined by resolving the `applicationPath`
   * to a partition. If the partition is a `GitPartition`, the name of its
   * repository is returned. If the resource does not belong to a `GitPartition`
   * [[scala.None]] is returned.
   *
   * @param requestPath
   * @param isGitRequest
   * @return
   */
  def getRepositoryName(requestPath: Path, isGitRequest: Boolean): Option[RepositoryName] = {
    if (isGitRequest) {
      val uri = requestPath.strip(Path(Config.get.gitMount)).toRelative
      val name = stripGitSuffixes(uri.asString, gitSuffixes)
      val rname = if (name.endsWith(".git")) name.substring(0, name.length-4) else name
      Some(RepositoryName(rname))
    } else {
      PubletWeb.publet.mountManager.resolveMount(requestPath)
        .map(_._2)
        .collect({ case t: GitPartition => t })
        .map(_.tandem.name)
    }
  }

  private def stripGitSuffixes(url: String, suffixes: List[String]): String = {
    suffixes match {
      case c::cs => {
        val idx = url.indexOf(c)
        if (idx > -1) stripGitSuffixes(url.substring(0, idx), cs)
        else stripGitSuffixes(url, cs)
      }
      case Nil => url
    }
  }
}