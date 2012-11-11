package org.eknet.publet.gitr.webui.scripts

import org.eknet.publet.engine.scala.ScalaScript
import GitrControl._
import org.eknet.publet.gitr.auth.{DefaultRepositoryStore, RepositoryModel, GitAction, RepositoryTag}
import org.eknet.publet.web.util.{PubletWeb, PubletWebContext}
import org.eknet.publet.gitr.GitRequestUtils

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 16.06.12 23:34
 */
class UpdateRepository extends ScalaScript {
  import org.eknet.publet.web.util.RenderUtils.makeJson

  def serve() = {
    getRepositoryModelFromParam flatMap { rm =>
      GitrControl.checkGitAction(GitAction.admin, rm)
      PubletWebContext.param("repoState") flatMap ( state => {
        PubletWeb.instance[DefaultRepositoryStore].get
          .updateRepository(RepositoryModel(rm.name, RepositoryTag.withName(state), rm.owner))
        makeJson(Map("success"->true, "message"->"Repository successfully updated."))
      })
    }
  }
}
