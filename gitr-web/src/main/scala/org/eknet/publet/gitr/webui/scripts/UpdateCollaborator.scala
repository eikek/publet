package org.eknet.publet.gitr.webui.scripts

import org.eknet.publet.engine.scala.ScalaScript
import ScalaScript._
import GitrControl._
import org.eknet.publet.web.util.{PubletWeb, PubletWebContext}
import org.eknet.publet.gitr.auth.GitAction
import org.eknet.publet.gitr.GitRequestUtils

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 17.06.12 17:29
 */
class UpdateCollaborator extends ScalaScript {

  def serve() = {
    getRepositoryModelFromParam flatMap { rm =>
      GitrControl.checkGitAction(GitAction.admin, rm)
      PubletWebContext.param("groupName") flatMap ( gn => {
        PubletWebContext.param("permissionName") flatMap (pm => {
          val authm = PubletWeb.authManager
          val action = GitAction.withName(pm.toLowerCase)
          val remove = PubletWebContext.param("do").getOrElse("add")
          if (remove == "add") {
            //TODO
//            authm.updatePermission(PermissionModel(action.toString, List(rm.name), List(gn)))
            success("Permission successflly updated.")
          } else {
//            authm.removePermission(gn, Permission(action.toString, Some(rm.name)))
            success("Permission successflly removed.")
          }
        })
      })
    } orElse (error("Insufficient arguments."))
  }

  private def success(msg: String) = makeJson(Map("success"->true, "message"-> msg))
  private def error(msg: String) = makeJson(Map("success"->true, "message" -> msg))
}
