package org.eknet.publet.gitr.web.scripts

import org.eknet.publet.engine.scala.ScalaScript
import ScalaScript._
import GitrControl._
import org.eknet.publet.auth.{GitAction, RepositoryTag, RepositoryModel}
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web.util.{PubletWeb, PubletWebContext}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 16.06.12 23:34
 */
class UpdateRepository extends ScalaScript {

  def serve() = {
    getRepositoryModelFromParam flatMap { rm =>
      Security.checkGitAction(GitAction.gitadmin, rm)
      PubletWebContext.param("repoState") flatMap ( state => {
        PubletWeb.authManager.updateRepository(RepositoryModel(rm.name, RepositoryTag.withName(state), rm.owner))
        makeJson(Map("success"->true, "message"->"Repository successfully updated."))
      })
    }
  }
}
