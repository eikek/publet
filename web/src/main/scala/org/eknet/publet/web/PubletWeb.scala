package org.eknet.publet.web

import filter.NotFoundHandler
import javax.servlet.ServletContext
import scripts._
import shiro.{UsersRealm, AuthManager}
import org.eknet.publet.partition.git.{GitPartMan, GitPartManImpl}
import org.eknet.publet.gitr.{GitrMan, GitrManImpl}
import org.eknet.publet.Publet
import org.eknet.publet.vfs.{ContentResource, ResourceName, Path}
import template.{ConfiguredScalateEngine, Templates}
import util.{PropertiesMap, AttributeMap, Context, Key}
import org.apache.shiro.web.mgt.DefaultWebSecurityManager
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver
import org.apache.shiro.web.filter.authc.{AnonymousFilter, BasicHttpAuthenticationFilter, FormAuthenticationFilter}
import org.apache.shiro.web.env.{EnvironmentLoader, DefaultWebEnvironment}
import javax.servlet.http.HttpServletResponse
import org.eknet.publet.engine.scalate.ScalateEngine
import org.fusesource.scalate.Binding
import org.eknet.publet.engine.scala.{ScriptCompiler, ScalaScriptEngine, DefaultPubletCompiler}
import java.io.File
import org.eknet.publet.vfs.util.MapContainer

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 20:02
 */
object PubletWeb {

  // initialized on context startup
  private var servletContextI: ServletContext = null
  private var contextMapI: AttributeMap = null

  private val contentRootRepo = Path("contentroot")

  //key definitions
  private val gitrKey = Key("gitr", {
    case Context => new GitrManImpl(Config.repositories)
  })

  private val gitPartKey = Key("gitpart", {
    case Context => new GitPartManImpl(contextMap(gitrKey).get)
  })

  private val contentRootKey = Key("contentroot", {
    case Context => contextMap(gitPartKey).get.getOrCreate(contentRootRepo, org.eknet.publet.partition.git.Config(None))
  })
  private val authManagerKey = Key("publetAuthManager", {
    case Context => new AuthManager()
  })

  private val scalateEngineKey = Key("scalateEngine", {
    case Context => {
      val e = new ConfiguredScalateEngine('wikiMain, publet)
      e.engine.combinedClassPath = true
      e.engine.importStatements ++= webImports.map("import "+ _)
      e.engine.classpath = ScriptCompiler.servletPath.mkString(File.pathSeparator)
      e.engine.bindings ++= List(Binding("includeLoader", "_root_."+classOf[IncludeLoader].getName, true))
      e.attributes = Map("includeLoader" -> new IncludeLoader)
      e
    }
  })

  private val webImports = List(
    "org.eknet.publet.web.PubletWeb",
    "org.eknet.publet.web.PubletWebContext",
    "org.eknet.publet.web.util.AttributeMap",
    "org.eknet.publet.web.util.Key",
    "org.eknet.publet.web.shiro.Security"
  )

  // initializes the Publet root container
  private val publetKey = Key("publet", {
    case Context => {
      val publ = Publet()
      publ.mountManager.mount(Path(Config.mainMount).toAbsolute, contextMap(contentRootKey).get)

      //scripts
      val cont = new MapContainer()
      cont.addResource(new WebScriptResource(ResourceName("login.json"), Login))
      cont.addResource(new WebScriptResource(ResourceName("logout.json"), Logout))
      publ.mountManager.mount(Path("/publet/scripts/"), cont)

      Templates.mountPubletResources(publ)
      publ
    }
  })

  def servletContext = servletContextI
  def contextMap = contextMapI
  lazy val publet = contextMap(publetKey).get
  lazy val scalateEngine = contextMap(scalateEngineKey).get
  lazy val gitr: GitrMan = contextMap(gitrKey).get
  lazy val gitpartman: GitPartMan = contextMap(gitPartKey).get
  lazy val contentRoot = contextMap(contentRootKey).get
  lazy val authManager = contextMap(authManagerKey).get
  lazy val publetSettings = new PropertiesMap {
    override def file = contentRoot.lookup(Path(Publet.allIncludes+"settings.properties"))
      .collect({case cc: ContentResource => cc})
      .map(_.inputStream)
  }

  /**
   * Returns the uri to the login page. The uri is either fetched
   * from the settings file or the default login page is returned.
   *
   * @return
   */
  def getLoginPath = PubletWeb.servletContext.getContextPath +
    publetSettings("publet.loginUrl").getOrElse("/publet/templates/login.html")

  lazy val notFoundHandlerKey = Key("notFoundHandler", {
    case Context => new NotFoundHandler {
      def resourceNotFound(path: Path, resp: HttpServletResponse) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND)
      }
    }
  })

  def notFoundHandler = contextMap(notFoundHandlerKey).get

  // ~~~ servlet context listener

  def initialize(sc: ServletContext) {
    this.servletContextI = sc
    this.contextMapI = AttributeMap(servletContext)
    Config.setContextPath(servletContext.getContextPath)

    publet.engineManager.register("/*", scalateEngine)
    //      val inclEngine = ScalateEngine('include, publ)
    //      inclEngine.disableLayout()
    //      publ.engineManager.addEngine(inclEngine)

    val compiler = new DefaultPubletCompiler(publet, Config.mainMount, webImports)
    val scalaEngine = new ScalaScriptEngine('eval, compiler, scalateEngine)
    publet.engineManager.addEngine(scalaEngine)

    //      val scriptInclude = new ScalaScriptEngine('evalinclude, compiler, inclEngine)
    //      publ.engineManager.addEngine(scriptInclude)

    contextMap(publetKey).get
    initShiro()
  }

  private def initShiro() {
    //construct securitymanager and filterchain
    val webenv = new DefaultWebEnvironment()

    val sm = new DefaultWebSecurityManager()
    sm.setRealm(new UsersRealm(authManager))
    webenv.setSecurityManager(sm)

    val resolver = new PathMatchingFilterChainResolver()
    val formauth = new FormAuthenticationFilter()
    formauth.setLoginUrl(getLoginPath)
    resolver.getFilterChainManager.addFilter("authc", formauth)
    resolver.getFilterChainManager.addFilter("authcBasic", new BasicHttpAuthenticationFilter)
    resolver.getFilterChainManager.addFilter("anon", new AnonymousFilter)

    val gitPath = Path(Config.gitMount).toAbsolute.asString + "/**"
    resolver.getFilterChainManager.createChain(gitPath, "authcBasic")
    resolver.getFilterChainManager.createChain("/**", "anon")
    webenv.setFilterChainResolver(resolver)
    servletContextI.setAttribute(EnvironmentLoader.ENVIRONMENT_ATTRIBUTE_KEY, webenv)
  }


  def destroy(sc: ServletContext) {
    this.servletContextI = null
    this.contextMapI = null
  }
}
