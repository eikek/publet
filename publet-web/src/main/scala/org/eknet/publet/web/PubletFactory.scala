package org.eknet.publet.web

import extensions.scripts.{SetEngine, Listing, CaptchaScript, MailContact}
import org.eknet.publet.engine.scalascript.{ScriptPartition, ScalaScriptEvalEngine}
import template._
import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.{Path, Publet}
import org.eknet.publet.resource.ContentType._
import org.eknet.publet.partition.git.GitPartition
import javax.servlet.ServletContext

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 23:15
 */
object PubletFactory {

  def createPublet(): Publet = {
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


    publ.mount(Path.root, WebContext().service(gitpartitionKey))
    publ.mount(Path("/.publets/scripts"), new ScriptPartition('scripts, scripts))

    publ
  }

  val gitpartitionKey = Key("gitPartition", () => {
    new GitPartition('publetroot, Config.contentRoot, "publetrepo")
  })

  private class WebScalaScriptEngine(name: Symbol, e: PubletEngine) extends ScalaScriptEvalEngine(name, e) {
    override def importPackages = super.importPackages ++ List(
    "org.eknet.publet.web.WebContext",
    "org.eknet.publet.web.AttributeMap",
    "org.eknet.publet.web.Key"
    )
  }

  private val scripts = Map(
    "mailcontact" -> (MailContact, html),
    "listing" -> (Listing, html),
    "captcha" -> (CaptchaScript, png),
    "setengine" -> (SetEngine, json)
  )
}
