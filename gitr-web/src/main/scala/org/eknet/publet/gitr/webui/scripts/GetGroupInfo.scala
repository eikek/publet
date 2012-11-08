package org.eknet.publet.gitr.webui.scripts

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web.util.PubletWeb

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 16.06.12 21:31
 */
class GetGroupInfo extends ScalaScript {

  def serve() = {
    Security.checkAuthenticated()
    val allGroups = Set[String]() // TODO PubletWeb.authManager.getAllGroups
    val repoGroups = GitrControl.getRepositoryModelFromParam map { r =>
      PubletWeb.authManager
//        .getAllPermissions
//        .withFilter(_.repository.contains(r.name))
//        .flatMap(r => r.roles.map(g=>Map("permission"->r.perm, "group" -> g)))
    }

    import ScalaScript._
    repoGroups.flatMap(v => makeJson(Map("collaborators" -> v))) orElse {
      makeJson(allGroups.toList)
    }
  }
}
