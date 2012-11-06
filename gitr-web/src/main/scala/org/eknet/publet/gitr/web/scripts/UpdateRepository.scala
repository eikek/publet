package org.eknet.publet.gitr.web.scripts

import org.eknet.publet.engine.scala.ScalaScript
import ScalaScript._
import GitrControl._
import org.eknet.publet.gitr.auth.{DefaultRepositoryStore, RepositoryModel, GitAction, RepositoryTag}
import org.eknet.publet.web.util.{PubletWeb, PubletWebContext}
import org.eknet.publet.gitr.GitRequestUtils

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 16.06.12 23:34
 */
class UpdateRepository extends ScalaScript {

  def serve() = {
    getRepositoryModelFromParam flatMap { rm =>
      GitRequestUtils.checkGitAction(GitAction.edit, rm)
      PubletWebContext.param("repoState") flatMap ( state => {
        PubletWeb.instance[DefaultRepositoryStore]
          .updateRepository(RepositoryModel(rm.name, RepositoryTag.withName(state), rm.owner))
        makeJson(Map("success"->true, "message"->"Repository successfully updated."))
      })
    }
  }
}
