/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.publet.web

import filter.{PageWriter, NotFoundHandler}
import javax.servlet.ServletContext
import scripts._
import shiro.{UsersRealm, AuthManager}
import org.eknet.publet.partition.git.{GitPartition, GitPartMan, GitPartManImpl}
import org.eknet.publet.gitr.{GitrMan, GitrManImpl}
import org.eknet.publet.Publet
import org.eknet.publet.vfs.{ContentResource, ResourceName, Path}
import template.{IncludeResourceLoader, IncludeLoader, ConfiguredScalateEngine, Templates}
import util.{PropertiesMap, AttributeMap, Context, Key}
import org.apache.shiro.web.mgt.DefaultWebSecurityManager
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver
import org.apache.shiro.web.filter.authc.{AnonymousFilter, BasicHttpAuthenticationFilter, FormAuthenticationFilter}
import org.apache.shiro.web.env.{EnvironmentLoader, DefaultWebEnvironment}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.fusesource.scalate.Binding
import org.eknet.publet.engine.scala.{ScriptCompiler, ScalaScriptEngine, DefaultPubletCompiler}
import java.io.File
import org.eknet.publet.vfs.util.MapContainer
import grizzled.slf4j.Logging
import org.eknet.publet.auth.RepositoryModel
import org.apache.shiro.cache.MemoryConstrainedCacheManager
import tools.nsc.util.ScalaClassLoader.URLClassLoader
import java.net.URL

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 20:02
 */
object PubletWeb extends Logging {

  /**
   * ServletContext Init-parameter.
   *
   * Comma or semicolon separated path of filenames or URLs
   * pointing to directories or jar files. Directories should end
   * with '/'.
   */
  val customClasspathInitParam = "custom-classpath"

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

  /**
   * Returns the custom classpath that is optionally specified using a init-parameter
   * with the servlet context.
   *
   * @return
   */
  private def getCustomClasspath = Option(servletContext.getInitParameter(customClasspathInitParam)).filterNot(_.isEmpty)

  private val scalateEngineKey = Key("scalateEngine", {
    case Context => {
      val e = new ConfiguredScalateEngine('wikiMain, publet)
      e.engine.combinedClassPath = true
      e.engine.importStatements ++= webImports.map("import "+ _)
      e.engine.classpath = ScriptCompiler.servletPath.mkString(File.pathSeparator)
      getCustomClasspath.map(cp => {
        e.engine.classpath = cp + File.pathSeparator + e.engine.classpath
        val urls = cp.split("\\s*[,;]\\s*").map(new URL(_)).toSeq
        e.engine.classLoader = new URLClassLoader(urls, e.engine.getClass.getClassLoader)
      })
      e.engine.bindings ++= List(
        Binding("includeLoader", "_root_."+classOf[IncludeLoader].getName, true)
      )
      e.attributes = Map("includeLoader" -> new IncludeLoader)
      e
    }
  })

  private val webImports = List(
    "org.eknet.publet.web.Config",
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
      Templates.mountLoadmaskJs(publ)
      publ
    }
  })

  def servletContext = servletContextI
  def contextMap = contextMapI
  def publet = contextMap(publetKey).get
  def scalateEngine = contextMap(scalateEngineKey).get
  def gitr: GitrMan = contextMap(gitrKey).get
  def gitpartman: GitPartMan = contextMap(gitPartKey).get
  def contentRoot = contextMap(contentRootKey).get
  def authManager = contextMap(authManagerKey).get
  lazy val publetSettings = new PropertiesMap {
    reload()
    override def file = contentRoot.lookup(Path(Publet.allIncludes+"config/settings.properties"))
      .collect({case cc: ContentResource => cc})
      .map(_.inputStream)
  }

  /**
   * Returns the uri to the login page. This is the inner-application
   * uri. Use `urlOf()` to create a URL.
   *
   * @return
   */
  def getLoginPath = publetSettings("publet.loginUrl").getOrElse("/publet/templates/login.html")

  def notFoundHandlerKey = Key("notFoundHandler", {
    case Context => new NotFoundHandler with PageWriter {
      def resourceNotFound(path: Path, req: HttpServletRequest, resp: HttpServletResponse) {
        writeError(HttpServletResponse.SC_NOT_FOUND, req, resp)
      }
    }
  })

  def notFoundHandler = contextMap(notFoundHandlerKey).get

  /**
   * Returns the [[org.eknet.publet.auth.RepositoryModel]] of the
   * repository which contains the resource of the given path. If
   * the resource is not within a git repository, [[scala.None]]
   * is returned.
   *
   * @param path
   * @return
   */
  def getRepositoryModel(path: Path): Option[RepositoryModel] = {
    val gitrepo = publet.mountManager.resolveMount(path)
      .map(_._2)
      .collect({ case t: GitPartition => t })
      .map(_.tandem.name)
    gitrepo.map { name =>
      authManager.getRepository(name.name)
    }
  }

  // ~~~ servlet context listener

  /**
   * Initializes the web app. the `loggerInit` function is invoked
   * as early as possible, after the `Config` object is set up, so
   * logging is available as soon as possible.
   *
   * @param sc
   * @param loggerInit
   */
  def initialize(sc: ServletContext, loggerInit: ()=>Unit) {
    this.servletContextI = sc
    this.contextMapI = AttributeMap(servletContext)
    Config.setContextPath(servletContext.getContextPath)
    loggerInit.apply()

    publet.engineManager.register("/**", scalateEngine)

    val additionalImports = List(
      "org.eknet.publet.web.util.RenderUtils",
      "RenderUtils._"
    )
    val compiler = new DefaultPubletCompiler(publet, Config.mainMount, getCustomClasspath, webImports ::: additionalImports)
    val scalaEngine = new ScalaScriptEngine('eval, compiler, scalateEngine)
    publet.engineManager.register("*.scala", scalaEngine)

    contextMap(publetKey).get
    initShiro()

    WebExtensionLoader.installWebExtensions()
  }

  private def initShiro() {
    //construct securitymanager and filterchain
    val webenv = new DefaultWebEnvironment()

    val sm = new DefaultWebSecurityManager()
    sm.setRealm(new UsersRealm(authManager))
    sm.setCacheManager(new MemoryConstrainedCacheManager)
    webenv.setSecurityManager(sm)

    val resolver = new PathMatchingFilterChainResolver()
    val formauth = new FormAuthenticationFilter()
    val loginPath = Config("publet.urlBase").getOrElse(servletContext.getContextPath) + getLoginPath
    formauth.setLoginUrl(loginPath)
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
