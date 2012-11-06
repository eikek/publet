package org.eknet.publet.gitr.web.scripts

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.web.util.{PubletWeb, PubletWebContext}
import org.eknet.publet.gitr.auth.{DefaultRepositoryStore, RepositoryModel, GitAction}
import org.eknet.gitr.{GitrMan, RepositoryName}
import org.eknet.publet.gitr.GitRequestUtils

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.06.12 21:19
 */
class TransferOwner extends ScalaScript {

  def repoStore = PubletWeb.instance[DefaultRepositoryStore]

  def serve() = {
    import ScalaScript._
    import GitrControl._

    getRepositoryFromParam flatMap (repo => {
      val model = getRepositoryModelFromParam.get
      GitRequestUtils.checkGitAction(GitAction.edit, model)

      PubletWebContext.param("owner") flatMap (newOwner => {

        PubletWeb.authManager.findUser(newOwner) match {
          case None => makeJson(Map("success"->false, "message" -> ("User '"+newOwner+"' does not exist!")))
          case Some(_) => {
            // need to move possibly
            val newsegs = repo.name.segments.map(n => if (n == model.owner) newOwner else n)
            val newName = RepositoryName(newsegs)
            PubletWeb.instance[GitrMan].rename(repo.name, newName)

            // need to change owner permission
            repoStore.removeRepository(model.name)

            val newmodel = RepositoryModel(newName, model.tag, newOwner)
            PubletWeb.instance[DefaultRepositoryStore].updateRepository(newmodel)

            makeJson(Map("success" -> true,
              "message" -> "Successfully moved ownership!",
              "redirect" -> PubletWebContext.urlOf(GitrControl.mountPoint+ "/")))
          }
        }
      }) orElse(makeJson(Map("success"->false, "message"->"No owner specified.")))
    }) orElse(makeJson(Map("success"->false, "message"->"No repository specified.")))
  }
}
