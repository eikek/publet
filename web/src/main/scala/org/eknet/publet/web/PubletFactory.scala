package org.eknet.publet.web

import scripts._
import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.engine.scalascript.ScalaScriptEvalEngine
import org.eknet.publet.Publet
import org.eknet.publet.partition.git.GitPartition
import org.eknet.publet.vfs.Path
import org.eknet.publet.vfs.virtual.MutableContainer
import template.{HighlightJs, StandardEngine}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 23:15
 */
object PubletFactory {

  def createPublet() = {
    val publ = Publet()

    val gp = new GitPartition(
      Config.contentRoot,
      "publetrepo",
      Config("git.pollInterval").getOrElse("1500").toInt,
      Path.root,
      Some(publ.rootContainer))

    val scriptRoot = Path("/publet/scripts/")

    publ.mountManager.mount(Path("/"+ Config.mainMount), gp)
    val cont = new MutableContainer(scriptRoot)
    cont.addResource(new WebScriptResource(scriptRoot/"toggleRepo.html", ToggleGitExport))
    publ.mountManager.mount(scriptRoot, cont)



    val defaultEngine = new StandardEngine(publ).init()
    HighlightJs.install(publ, defaultEngine)
    publ.engineManager.register("/*", defaultEngine)
    publ.engineManager.addEngine(defaultEngine.includeEngine)

    val scalaEngine = new WebScalaScriptEngine('eval, defaultEngine)
    publ.engineManager.addEngine(scalaEngine)

    val scriptInclude = new WebScalaScriptEngine('evalinclude, defaultEngine.includeEngine)
    publ.engineManager.addEngine(scriptInclude)

    (publ, gp, defaultEngine)
  }

  private class WebScalaScriptEngine(name: Symbol, e: PubletEngine) extends ScalaScriptEvalEngine(name, e) {
    override def importPackages = super.importPackages ++ List(
    "org.eknet.publet.web.WebContext",
    "org.eknet.publet.web.util.AttributeMap",
    "org.eknet.publet.web.util.Key",
    "org.apache.shiro.{SecurityUtils => Security}"
    )
  }

}
