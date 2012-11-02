package org.eknet.publet.gitr.web.scripts

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.auth.repository.{RepositoryModel, GitAction}
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.gitr.RepositoryName
import org.eknet.publet.web.util.{PubletWeb, PubletWebContext}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.06.12 21:19
 */
class TransferOwner extends ScalaScript {
  def serve() = {
    import ScalaScript._
    import GitrControl._

    getRepositoryFromParam flatMap (repo => {
      val model = getRepositoryModelFromParam.get
      Security.checkGitAction(GitAction.gitadmin, model)

      PubletWebContext.param("owner") flatMap (newOwner => {

        PubletWeb.authManager.findUser(newOwner) match {
          case None => makeJson(Map("success"->false, "message" -> ("User '"+newOwner+"' does not exist!")))
          case Some(_) => {
            // need to move possibly
            val newsegs = repo.name.segments.map(n => if (n == model.owner) newOwner else n)
            val newName = RepositoryName(newsegs)
            PubletWeb.gitr.rename(repo.name, newName)

            // need to change owner permission
            PubletWeb.authManager.removeRepository(model.name)

            val newmodel = RepositoryModel(newName.strip.name, model.tag, newOwner)
            PubletWeb.authManager.updateRepository(newmodel)

            makeJson(Map("success" -> true,
              "message" -> "Successfully moved ownership!",
              "redirect" -> PubletWebContext.urlOf(GitrControl.mountPoint+ "/")))
          }
        }
      }) orElse(makeJson(Map("success"->false, "message"->"No owner specified.")))
    }) orElse(makeJson(Map("success"->false, "message"->"No repository specified.")))
  }
}
