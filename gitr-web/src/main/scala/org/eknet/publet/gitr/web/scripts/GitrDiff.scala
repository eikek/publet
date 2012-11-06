package org.eknet.publet.gitr.web.scripts

import org.eknet.publet.engine.scala.ScalaScript
import GitrControl._
import java.io.ByteArrayOutputStream
import org.eknet.publet.vfs.{ContentType, Content}
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web.util.PubletWebContext
import org.eknet.publet.gitr.auth.GitAction
import org.eknet.publet.gitr.GitRequestUtils

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
      getRepositoryModelFromParam.map(GitRequestUtils.checkGitAction(GitAction.pull, _))
      getCommitFromRequest(repo) map ( commit => {
        val baos = new ByteArrayOutputStream()
        val df = new HtmlDiffFormatter(baos)
        repo.formatDiff(commit, df, None, PubletWebContext.param(pParam))
        Content(df.getHtml, ContentType.html)
      })
    })
  }
}
