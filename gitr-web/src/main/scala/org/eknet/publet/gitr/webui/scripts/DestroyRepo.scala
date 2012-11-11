package org.eknet.publet.gitr.webui.scripts

import org.eknet.publet.engine.scala.ScalaScript
import grizzled.slf4j.Logging
import org.eknet.publet.web.util.{RenderUtils, PubletWebContext, PubletWeb}
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
    import RenderUtils.makeJson

    getRepositoryModelFromParam flatMap ( model => {
      checkGitAction(GitAction.admin, model)
      try {
        gitrMan.delete(model.name)
        repoStore.removeRepository(model.name)
        info("Successfully destroyed repository: "+ model.name.fullName)
        makeJson(Map("success"->true,
          "message"->"Successfully destroyed the repository.",
          "redirect"->PubletWebContext.urlOf(mountPoint+"/")))
      }
      catch {
        case e:Exception => {
          error("Error destroying repository '"+ model.name.fullName +"'!", e)
          makeJson(Map("success"->false, "message"-> ("Unable to destroy the repository. "+ e.getLocalizedMessage)))
        }
      }
    }) orElse(makeJson(Map("success"->false, "message"->"Repository not found.")))
  }

  private def repoStore = PubletWeb.instance[DefaultRepositoryStore].get
  private def gitrMan = PubletWeb.instance[GitrMan].get
}