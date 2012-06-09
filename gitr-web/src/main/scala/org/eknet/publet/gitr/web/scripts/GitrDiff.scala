package org.eknet.publet.gitr.web.scripts

import org.eknet.publet.engine.scala.ScalaScript
import GitrControl._
import java.io.ByteArrayOutputStream
import org.eknet.publet.web.PubletWebContext
import org.eknet.publet.vfs.{ContentType, Content}

/**
 * Formats the diff in html code that is loaded from
 * the client into the page.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.06.12 16:33
 */
class GitrDiff extends ScalaScript {

  def serve() = {
    getRepositoryFromParam flatMap ( repo => {
      getCommitFromRequest(repo) map ( commit => {
        val baos = new ByteArrayOutputStream()
        val df = new HtmlDiffFormatter(baos)
        repo.formatDiff(commit, df, None, PubletWebContext.param(pParam))
        Content(df.getHtml, ContentType.html)
      })
    })
  }
}
