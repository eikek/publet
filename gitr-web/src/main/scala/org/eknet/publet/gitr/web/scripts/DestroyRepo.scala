package org.eknet.publet.gitr.web.scripts

import org.eknet.publet.engine.scala.ScalaScript
import grizzled.slf4j.Logging
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.auth.GitAction
import org.eknet.publet.web.util.{PubletWebContext, PubletWeb}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.06.12 20:51
 */
class DestroyRepo extends ScalaScript with Logging {

  def serve() = {
    import GitrControl._
    import ScalaScript._

    getRepositoryFromParam flatMap ( repo => {
      val model = getRepositoryModelFromParam.get
      Security.checkGitAction(GitAction.gitadmin, model)
      try {
        PubletWeb.gitr.delete(repo.name)
        PubletWeb.authManager.removeRepository(repo.name.name)
        info("Successfully destroyed repository: "+ repo.name)
        makeJson(Map("success"->true,
          "message"->"Successfully destroyed the repository.",
          "redirect"->PubletWebContext.urlOf(GitrControl.mountPoint+"/")))
      }
      catch {
        case e:Exception => {
          error("Error destroying repository '"+ repo.name +"'!", e)
          makeJson(Map("success"->false, "message"-> ("Unable to destroy the repository. "+ e.getLocalizedMessage)))
        }
      }
    }) orElse(makeJson(Map("success"->false, "message"->"Repository not found.")))
  }
}