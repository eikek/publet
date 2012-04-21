package org.eknet.publet.web.extensions.scripts

import org.eknet.publet.engine.scalascript.ScalaScript
import org.eknet.publet.web.{PubletFactory, WebContext}
import java.io.File
import ScalaScript._
import PubletFactory._
import WebContext._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.04.12 21:36
 */
object ToggleGitExport extends ScalaScript {

  private val file = "git-daemon-export-ok"

  def serve() = {
    val ctx = WebContext()
    import ctx._

    val exportok = new File(service(gitpartitionKey).repository, file)
    if (exportok.exists()) {
      exportok.delete()
      makeHtml("<p class=\"box success\">Content repository not exported.</p>")
    } else {
      exportok.createNewFile()
      makeHtml("<p class=\"box success\">Content repository now " +
        "available under <code>"+ ctx(contextUrl).get +"/git/publetrepo.git</code>.</p>")
    }
  }

}
