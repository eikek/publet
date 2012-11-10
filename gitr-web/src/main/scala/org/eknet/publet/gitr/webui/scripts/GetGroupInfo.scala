package org.eknet.publet.gitr.webui.scripts

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web.util.PubletWeb
import scala.collection.mutable.ListBuffer
import org.eknet.publet.gitr.auth.{GitAction, GitPermission, RepositoryModel}

/**
 * Returns a list of groups that have push or pull permission
 * granted for a repository.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 16.06.12 21:31
 */
class GetGroupInfo extends ScalaScript {

  def serve() = {
    Security.checkAuthenticated()
    val authm = PubletWeb.authManager

    val repoGroups = GitrControl.getRepositoryModelFromParam map { r =>
      val permFilter = filterGitPermission(r)_
      authm.allGroups
        .map(g => (g -> permFilter(authm.getPermissions(g))))
        .withFilter(t => t._2 != None)
        .map(t => Map("group" -> t._1, "permission" -> t._2.get))
        .toArray
    }

    import ScalaScript._
    repoGroups.flatMap(v => makeJson(Map("collaborators" -> v))) orElse {
      makeJson(authm.allGroups.toArray)
    }
  }

  private def filterGitPermission(r: RepositoryModel)(perms: Set[String]) = {
    val actions = perms.withFilter(GitPermission.isValid)
      .map(new GitPermission(_))
      .withFilter(gp => gp.repositories.contains(r.name.name) || gp.repositories.contains("*"))
      .withFilter(gp => gp.actions.contains(GitAction.pull.name) || gp.actions.contains(GitAction.push.name))
      .map(gp => gp.actions)
      .flatten
    if (actions.contains(GitAction.push.name))
      Some(GitAction.push.name)
    else if (actions.contains(GitAction.pull.name))
      Some(GitAction.pull.name)
    else None
  }
}
