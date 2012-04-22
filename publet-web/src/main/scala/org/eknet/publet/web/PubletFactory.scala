package org.eknet.publet.web

import extensions.scripts._
import org.eknet.publet.engine.scalascript.{ScriptPartition, ScalaScriptEvalEngine}
import template._
import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.{Path, Publet}
import org.eknet.publet.resource.ContentType._
import org.eknet.publet.resource.Partition

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 23:15
 */
object PubletFactory {

  def createPublet(rootPartition: Partition): Publet = {
    val publ = Publet()

    val defaultEngine = new DefaultEngine(publ)
    publ.register("/*", defaultEngine)
    publ.addEngine(defaultEngine.includeEngine)

    val editEngine = new HtmlTemplateEngine('edit, EditEngine) with FilebrowserTemplate
    publ.addEngine(editEngine)

    val scalaEngine = new WebScalaScriptEngine('eval, defaultEngine)
    publ.addEngine(scalaEngine)

    val scriptInclude = new WebScalaScriptEngine('evalinclude, defaultEngine.includeEngine)
    publ.addEngine(scriptInclude)


    publ.mount(Path("/"+ Config.mainMount), rootPartition)
    publ.mount(Path("/.publets/scripts"), new ScriptPartition('scripts, scripts))

    publ
  }

  private class WebScalaScriptEngine(name: Symbol, e: PubletEngine) extends ScalaScriptEvalEngine(name, e) {
    override def importPackages = super.importPackages ++ List(
    "org.eknet.publet.web.WebContext",
    "org.eknet.publet.web.util.AttributeMap",
    "org.eknet.publet.web.util.Key",
    "org.eknet.publet.web.extensions.WebDsl",
    "org.apache.shiro.SecurityUtils"
    )
  }

  private val scripts = Map(
    "mailcontact" -> (MailContact, html),
    "listing" -> (Listing, html),
    "captcha" -> (CaptchaScript, png),
    "setengine" -> (SetEngine, json),
    "toggleRepo" -> (ToggleGitExport, html)
  )
}
