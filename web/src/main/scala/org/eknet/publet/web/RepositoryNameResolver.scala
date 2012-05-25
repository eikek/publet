package org.eknet.publet.web

import util.{Request, Key}
import org.eclipse.jgit.http.server.GitSmartHttpTools
import org.eknet.publet.partition.git.GitPartition
import org.eknet.publet.gitr.RepositoryName
import org.eknet.publet.vfs.Path
import org.eknet.publet.auth.{RepositoryTag, RepositoryModel}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 22:30
 */
trait RepositoryNameResolver {
  this: RequestUrl =>


  private val gitReceivePack = "/" + GitSmartHttpTools.RECEIVE_PACK
  private val gitUploadPack = "/" + GitSmartHttpTools.UPLOAD_PACK
  private val gitSuffixes = List(gitReceivePack, gitUploadPack, "/info/refs", "/HEAD", "/objects")

  private val gitRepositoryNameKey = Key("gitRepositoryName", {
    case Request => {
      if (isGitRequest) {
        val uri = applicationPath.strip(Path(Config.gitMount)).toRelative
        val name = stripGitSuffixes(uri.asString, gitSuffixes)
        val rname = if (name.endsWith(".git")) name.substring(0, name.length-4) else name
        Some(RepositoryName(rname))
      } else {
        PubletWeb.publet.mountManager.resolveMount(applicationPath)
          .map(_._2)
          .collect({ case t: GitPartition => t })
          .map(_.tandem.name)
      }
    }
  })

  /**
   * The name of the repository the current request points to.
   * @return
   */
  def getRepositoryName = PubletWebContext.attr(gitRepositoryNameKey).get

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

  private val repositoryModelKey = Key("requestRepositoryModel", {
    case Request => getRepositoryName.map { name =>
      PubletWeb.authManager
        .getAllRepositories
        .find(_.name == name.name)
        .getOrElse(RepositoryModel(name.name, RepositoryTag.open))
    }
  })

  /**Returns the current repository and its state. If no state
   * is defined, the repository is considered [[org.eknet.publet.auth.RepositoryTag.open]]
   * If the current request does not point to an repository,
   * [[scala.None]] is returned.
   *
   * @return
   */
  def getRepositoryModel = PubletWebContext.attr(repositoryModelKey).get

  private val gitActionKey = Key("gitrequestAction", {
    case Request => {
      if (isGitRequest) {
        val gitp = fullUrl.substring(Config.gitMount.length)
        if (gitp.endsWith(gitReceivePack))
          Some(GitAction.push)
        else if (gitp.endsWith(gitUploadPack))
          Some(GitAction.pull)
        else if (gitp.contains("?service=git-receive-pack"))
          Some(GitAction.push)
        else if (gitp.contains("?service=git-upload-pack"))
          Some(GitAction.pull)
        else
          Some(GitAction.pull)
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
  def getGitAction: Option[GitAction.Value] = PubletWebContext.attr(gitActionKey).get

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
  def containerRequestUri = PubletWebContext.attr(containerRequestUriKey).get

}
