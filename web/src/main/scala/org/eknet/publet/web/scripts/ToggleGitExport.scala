package org.eknet.publet.web.scripts

import org.eknet.publet.engine.scala.ScalaScript
import ScalaScript._
import org.eknet.publet.web.{WebContext, PubletFactory, WebPublet}
import WebContext._
import org.eknet.publet.web.shiro.Security

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.04.12 21:36
 */
object ToggleGitExport extends ScalaScript {

  private val exportOkMsg = "Content repository now available under %s."
  private val noExportMsg = "Content repository not exported."

  def targetType = WebContext().requestPath.name.targetType

  def gitRepoUrl = WebContext(contextUrl).get +"/git/publetrepo.git"

  def isExported = {
    val gitpartman = WebPublet().gitPartMan
    gitpartman.isExportOk(PubletFactory.mainRepoPath)
  }

  def markup() = {
    val msgOk = <span>Content repository now available under <code>{gitRepoUrl}</code>.</span>
    val msgOff = <span>Content repository not exported.</span>
    val exported = isExported
    makeHtml {
      <p class="box warning">
        { if (exported) msgOk else msgOff }
        <form action={ WebContext().requestPath.asString } method="post">
          <input type="hidden" name="toggle"/>
          <button class="ym-button ym-star" type="submit">{ if (exported) "Make repository private" else "Publish repository" }</button>
        </form>
      </p>
    }
  }

  def serve() = {
    WebContext().parameter("toggle") match {
      case Some(_) => {
        Security.checkPerm("git:export", PubletFactory.mainRepoPath)
        val gitpartman = WebPublet().gitPartMan
        val exported = gitpartman.isExportOk(PubletFactory.mainRepoPath)
        gitpartman.setExportOk(PubletFactory.mainRepoPath, !exported)
      }
      case None =>
    }
    markup()
  }

}
