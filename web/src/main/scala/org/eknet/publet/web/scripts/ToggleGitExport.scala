package org.eknet.publet.web.scripts

import org.eknet.publet.engine.scalascript.ScalaScript
import org.eknet.publet.web.WebContext
import java.io.File
import ScalaScript._
import WebContext._
import org.eknet.publet.vfs.ContentType

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.04.12 21:36
 */
object ToggleGitExport extends ScalaScript {

  private val file = "git-daemon-export-ok"

  private val exportOkMsg = "Content repository now available under %s."
  private val noExportMsg = "Content repository not exported."

  def targetType = WebContext().requestPath.name.targetType

  def gitRepoUrl = WebContext(contextUrl).get +"/git/publetrepo.git"

  def serve() = {
    val ctx = WebContext()
    import ctx._

    val exportok = new File(service(webPubletKey).gitPartition.repository, file)
    if (exportok.exists()) {
      exportok.delete()
      if (targetType == ContentType.json) {
        makeJson(Map("success" -> true, "message" -> noExportMsg))
      } else {
        makeHtml(<p class="box success">{ noExportMsg }</p>)
      }
    } else {
      exportok.createNewFile()
      log.info("Exported publet git repository.")
      if (targetType == ContentType.json) {
        makeJson(Map("success"->true, "message"->String.format(exportOkMsg, gitRepoUrl)))
      } else {
        val repo = "<code>"+ gitRepoUrl +"</code>"
        makeHtml("<p class='box success'>"+ String.format(exportOkMsg, repo) +"</p>")
      }
    }
  }

}
