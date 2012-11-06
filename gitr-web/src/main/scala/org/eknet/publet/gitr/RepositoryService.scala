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

import com.google.inject.{Inject, Singleton}
import org.eclipse.jgit.http.server.GitSmartHttpTools
import org.eknet.publet.Publet
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.Config
import org.eknet.publet.gitr.auth.{DefaultRepositoryStore, RepositoryModel, GitAction}
import org.eknet.gitr.RepositoryName
import org.eknet.publet.gitr.partition.GitPartition

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.11.12 19:37
 */
@Singleton
class RepositoryService @Inject() (publet: Publet, config: Config, authm: DefaultRepositoryStore) {

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
  def getGitRequestAction(fullUrl: String): GitAction.Action = {
    val gitp = fullUrl.substring(GitRequestUtils.gitMount(config).length)
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
      val uri = requestPath.strip(Path(GitRequestUtils.gitMount(config))).toRelative
      val name = stripGitSuffixes(uri.asString, gitSuffixes)
      Some(RepositoryName(name))
    } else {
      publet.mountManager.resolveMount(requestPath)
        .map(_._2)
        .collect({ case t: GitPartition => t })
        .map(_.tandem.name)
    }
  }

  /**
   * Returns the [[org.eknet.publet.gitr.auth.RepositoryModel]] of the
   * repository which contains the resource of the given path. If
   * the resource is not within a git repository, [[scala.None]]
   * is returned.
   *
   * @param path
   * @return
   */
  def getRepositoryModel(path: Path): Option[RepositoryModel] = {
    val gitrepo = publet.mountManager.resolveMount(path)
      .map(_._2)
      .collect({ case t: GitPartition => t })
      .map(_.tandem.name)
    gitrepo.map { name => authm.getRepository(name) }
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
