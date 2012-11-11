package org.eknet.publet.gitr.webui.scripts

import org.eknet.publet.engine.scala.ScalaScript
import GitrControl._
import org.eknet.publet.web.util.{PubletWeb, PubletWebContext}
import org.eknet.publet.gitr.auth.{GitPermissionBuilder, GitAction}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 17.06.12 17:29
 */
class UpdateCollaborator extends ScalaScript with GitPermissionBuilder {
  import org.eknet.publet.web.util.RenderUtils.makeJson

  def param(name: String) = PubletWebContext.param(name)

  def serve() = {
    getRepositoryModelFromParam flatMap { rm =>
      GitrControl.checkGitAction(GitAction.admin, rm)
      (param("groupName"), param("permissionName")) match {
        case (Some(gn), Some(pm)) => {
          val authm = PubletWeb.authManager
          val action = pm.toLowerCase
          val remove = PubletWebContext.param("do").getOrElse("add")
          if (remove == "add") {
            authm.addPermission(gn, git grant(action) on(rm.name))
            success("Permission successflly updated.")
          } else {
            authm.dropPermission(gn, git grant(action) on(rm.name))
            success("Permission successflly removed.")
          }
        }
        case _ => error("Insufficient arguments.")
      }
    }
  }

  private def success(msg: String) = makeJson(Map("success"->true, "message"-> msg))
  private def error(msg: String) = makeJson(Map("success"->true, "message" -> msg))
}
