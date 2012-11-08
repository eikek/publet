package org.eknet.publet.gitr.webui.scripts

import org.eknet.publet.engine.scala.ScalaScript
import grizzled.slf4j.Logging
import org.eknet.publet.web.util.{PubletWebContext, PubletWeb}
import org.eknet.publet.gitr.GitRequestUtils
import org.eknet.publet.gitr.auth.{DefaultRepositoryStore, GitAction}
import org.eknet.gitr.GitrMan

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
      GitrControl.checkGitAction(GitAction.admin, model)
      try {
        PubletWeb.instance[GitrMan].get.delete(repo.name)
        PubletWeb.instance[DefaultRepositoryStore].get.removeRepository(repo.name)
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