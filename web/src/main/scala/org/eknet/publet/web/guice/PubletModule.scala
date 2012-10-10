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

package org.eknet.publet.web.guice

import com.google.inject._
import internal.ProviderMethod
import org.eknet.publet.web.template.{IncludeLoader, ConfiguredScalateEngine}
import org.eknet.publet.engine.scala.{ScalaScriptEngine, DefaultPubletCompiler, ScriptCompiler}
import java.io.File
import java.net.URL
import tools.nsc.util.ScalaClassLoader.URLClassLoader
import org.fusesource.scalate.Binding
import org.eknet.publet.Publet
import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.vfs.{Container, ResourceName, Path}
import org.eknet.publet.web.{WebExtensionLoader, Settings, Config}
import org.eknet.publet.vfs.util.MapContainer
import org.eknet.publet.web.scripts.{Logout, Login, WebScriptResource}
import org.eknet.publet.partition.git.{GitPartManImpl, GitPartMan}
import org.eknet.publet.gitr.{GitrMan, GitrManImpl}
import org.eknet.publet.auth.PubletAuth
import org.eknet.publet.web.shiro.AuthManager
import javax.servlet.ServletContext
import com.google.inject.servlet.ServletModule
import com.google.inject.name.Named
import org.eknet.publet.engine.scalate.ScalateEngine
import org.eknet.publet.web.asset.impl.DefaultAssetManager
import org.eknet.publet.web.asset.AssetManager
import com.google.common.eventbus.EventBus
import com.google.inject.matcher.Matchers
import com.google.inject.spi.{InjectionListener, TypeEncounter, TypeListener}
import org.eknet.publet.web.util.StringMap
import java.util.EventListener

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.10.12 23:19
 */
class PubletModule(servletContext: ServletContext, init: Option[(EventBus, Config, WebExtensionLoader)]) extends ServletModule {

  private val webImports = List(
    "org.eknet.publet.web.Config",
    "org.eknet.publet.web.Settings",
    "org.eknet.publet.web.PubletWeb",
    "org.eknet.publet.web.PubletWebContext",
    "org.eknet.publet.web.util.AttributeMap",
    "org.eknet.publet.web.util.Key",
    "org.eknet.publet.web.shiro.Security"
  )
  private val contentRootRepo = Path("contentroot")

  /**
   * ServletContext Init-parameter.
   *
   * Comma or semicolon separated path of filenames or URLs
   * pointing to directories or jar files. Directories should end
   * with '/'.
   */
  val customClasspathInitParam = "custom-classpath"


  override def configureServlets() {
    bind(classOf[ServletContext])
      .annotatedWith(Names.servletContext)
      .toInstance(servletContext)

    val eventBus = init.map(_._1).getOrElse(new EventBus("Publet Global EventBus"))
    val config = init.map(_._2).getOrElse(new Config(servletContext.getContextPath, eventBus))
    bind(classOf[Config]) toInstance(config)

    bind(classOf[EventBus]).toInstance(eventBus)
    bindListener(Matchers.any(), new TypeListener {
      def hear[I](`type`: TypeLiteral[I], encounter: TypeEncounter[I]) {
        encounter.register(new InjectionListener[I] {
          def afterInjection(injectee: I) {
            eventBus.register(injectee)
          }
        })
      }
    })
    bind(classOf[WebExtensionLoader]) toInstance(init.map(_._3).getOrElse(new WebExtensionLoader(config)))
    bind(classOf[StringMap]) annotatedWith(Names.settings) to classOf[Settings] in Scopes.SINGLETON
    install(PubletShiroModule)
  }

  @Provides@Singleton
  def createAuthManager: PubletAuth = new AuthManager

  @Provides@Singleton
  def createGitrManager(config: Config): GitrMan = new GitrManImpl(config.repositories)

  @Provides@Singleton
  def createGitPartitionManager(gitr: GitrMan): GitPartMan = new GitPartManImpl(gitr)

  @Provides@Singleton@Named("contentroot")
  def createMainPartition(gitr: GitPartMan): Container =
    gitr.getOrCreate(contentRootRepo, org.eknet.publet.partition.git.Config(None))

  @Provides@Singleton
  def createPublet(@Named("contentroot") mainPartition: Container, config: Config) = {
    val publ = Publet()
    publ.mountManager.mount(Path(config.mainMount).toAbsolute, mainPartition)

    //scripts
    val cont = new MapContainer()
    cont.addResource(new WebScriptResource(ResourceName("login.json"), Login))
    cont.addResource(new WebScriptResource(ResourceName("logout.json"), Logout))
    publ.mountManager.mount(Path("/publet/scripts/"), cont)

    publ
  }

  @Provides@Singleton
  def createScalateEngine(publet: Publet, @Named("publetServletContext") servletContext: ServletContext): ScalateEngine = {
    val e = new ConfiguredScalateEngine('wikiMain, publet)
    e.engine.combinedClassPath = true
    e.engine.importStatements ++= webImports.map("import "+ _)
    e.engine.classpath = ScriptCompiler.servletPath.mkString(File.pathSeparator)
    getCustomClasspath(servletContext).map(cp => {
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

  @Provides@Singleton@Named("ScriptEngine")
  def createScriptEngine(publet: Publet, scalateEngine: ScalateEngine, @Named("publetServletContext") servletContext: ServletContext, config: Config): PubletEngine = {
    val additionalImports = List(
      "org.eknet.publet.web.util.RenderUtils",
      "RenderUtils._"
    )
    val compiler = new DefaultPubletCompiler(publet, config.mainMount,
      getCustomClasspath(servletContext), webImports ::: additionalImports)
    val scalaEngine = new ScalaScriptEngine('eval, compiler, scalateEngine)

    scalaEngine
  }

  @Provides@Singleton
  def createAssetManager(publet: Publet, config: Config) = {
    val tempDir = config.newStaticTempDir("assets")
    val mgr: AssetManager = new DefaultAssetManager(publet, tempDir)
    mgr
  }

  /**
   * Returns the custom classpath that is optionally specified using a init-parameter
   * with the servlet context.
   *
   * @return
   */
  private def getCustomClasspath(servletContext: ServletContext) =
    Option(servletContext.getInitParameter(customClasspathInitParam)).filterNot(_.isEmpty)

}