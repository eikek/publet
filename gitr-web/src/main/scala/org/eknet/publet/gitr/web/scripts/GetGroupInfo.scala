package org.eknet.publet.gitr.web.scripts

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.PubletWeb

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 16.06.12 21:31
 */
class GetGroupInfo extends ScalaScript {

  def serve() = {
    val allGroups = PubletWeb.authManager.getAllGroups
    val repoGroups = GitrControl.getRepositoryModelFromParam map { r =>
      PubletWeb.authManager
        .getAllPermissions
        .withFilter(_.repository.contains(r.name))
        .flatMap(r => r.roles.map(g=>Map("permission"->r.perm, "group" -> g)))
    }

    import ScalaScript._
    repoGroups.flatMap(v => makeJson(Map("collaborators" -> v.toList))) orElse {
      makeJson(allGroups.toList)
    }
  }
}
