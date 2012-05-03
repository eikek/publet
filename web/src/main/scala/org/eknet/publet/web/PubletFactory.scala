package org.eknet.publet.web

import scripts._
import org.eknet.publet.Publet
import org.eknet.publet.partition.git.GitPartition
import org.eknet.publet.vfs.util.MapContainer
import template.{HighlightJs, StandardEngine}
import org.eknet.publet.engine.scala.{DefaultPubletCompiler, ScalaScriptEngine}
import org.eknet.publet.vfs.{ResourceName, Path}

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
      Config("git.pollInterval").getOrElse("1500").toInt)

    val scriptRoot = Path("/publet/scripts/")

    publ.mountManager.mount(Path("/"+ Config.mainMount), gp)
    val cont = new MapContainer()
    cont.addResource(new WebScriptResource(ResourceName("toggleRepo.html"), ToggleGitExport))
    cont.addResource(new WebScriptResource(ResourceName("login.html"), Login))
    cont.addResource(new WebScriptResource(ResourceName("logout.html"), Logout))
    publ.mountManager.mount(scriptRoot, cont)

    val defaultEngine = new StandardEngine(publ).init()
    HighlightJs.install(publ, defaultEngine)
    publ.engineManager.register("/*", defaultEngine)
    publ.engineManager.addEngine(defaultEngine.includeEngine)

    val compiler = new DefaultPubletCompiler(publ, Config.mainMount, webImports)
    val scalaEngine = new ScalaScriptEngine('eval, compiler, defaultEngine)
    publ.engineManager.addEngine(scalaEngine)

    val scriptInclude = new ScalaScriptEngine('evalinclude, compiler, defaultEngine.includeEngine)
    publ.engineManager.addEngine(scriptInclude)

    (publ, gp, defaultEngine)
  }

  def webImports = List(
    "org.eknet.publet.web.WebContext",
    "org.eknet.publet.web.util.AttributeMap",
    "org.eknet.publet.web.util.Key",
    "org.eknet.publet.web.shiro.Security"
  )
}
