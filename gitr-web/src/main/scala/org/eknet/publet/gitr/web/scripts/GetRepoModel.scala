package org.eknet.publet.gitr.web.scripts

import org.eknet.publet.engine.scala.ScalaScript

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 16.06.12 21:27
 */
class GetRepoModel extends ScalaScript {
  def serve() = {
    import GitrControl._
    import ScalaScript._

    getRepositoryModelFromParam flatMap { r =>
      makeJson(Map("success"->true, "name"->r.name, "tag"->(r.tag.toString), "owner"->r.owner))
    } orElse {
      makeJson(Map("success"->false, "message"->"No repository specified."))
    }
  }
}
