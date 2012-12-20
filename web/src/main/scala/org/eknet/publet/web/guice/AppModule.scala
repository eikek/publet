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

import _root_.com.google.common.eventbus.EventBus
import _root_.com.google.inject.matcher.Matchers
import _root_.com.google.inject.name.{Names, Named}
import _root_.com.google.inject.{Provides, Scopes}
import _root_.com.google.inject.servlet.ServletModule
import org.eknet.publet.web.template.{DefaultLayout, IncludeLoader, ConfiguredScalateEngine}
import org.eknet.publet.engine.scala.{PubletCompiler, ScalaScriptEngine, DefaultPubletCompiler, ScriptCompiler}
import java.io.File
import java.net.URL
import tools.nsc.util.ScalaClassLoader.URLClassLoader
import org.fusesource.scalate.Binding
import org.eknet.publet.Publet
import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.vfs.{Container, ResourceName, Path}
import org.eknet.publet.vfs.util.MapContainer
import org.eknet.publet.web.scripts.{StartupScriptLoader, Logout, Login, WebScriptResource}
import javax.servlet.ServletContext
import org.eknet.publet.engine.scalate.ScalateEngine
import org.eknet.publet.web.asset.impl.DefaultAssetManager
import org.eknet.publet.web.asset.AssetManager
import org.eknet.publet.web.util.{AppSignature, PubletWeb}
import grizzled.slf4j.Logging
import org.eknet.guice.squire.SquireBinder
import org.eknet.publet.auth.guice.AuthModule
import org.eknet.publet.web.req.{PubletHandlerFactory, AssetsHandlerFactory, PubletMainFilter}
import org.eknet.publet.web.{Settings, PartitionMount, FilesystemMounter, PartitionMounter, Config}

/**
 * The main application module. It defines all bindings for a working application.
 *
 * Additional bindings can be supplied via the [[java.util.ServiceLoader]] pattern. Create
 * modules that extend the marker trait [[org.eknet.publet.web.guice.PubletModule]] and make
 * them available in `META-INF/services/org.eknet.publet.web.guice.PubletModule`. They are
 * then picked up by this module and installed.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.10.12 23:19
 */
class AppModule(servletContext: ServletContext) extends ServletModule with PubletBinding with Logging with SquireBinder {

  val name = "Core App Module"
  val license = AppSignature.license
  val version = AppSignature.version
  val homePage = Some(new URL("https://eknet.org/main/projects/publet/"))

  private val webImports = List(
    "org.eknet.publet.web.Config",
    "org.eknet.publet.web.Settings",
    "org.eknet.publet.web.util.PubletWeb",
    "org.eknet.publet.web.util.PubletWebContext",
    "org.eknet.publet.web.util.AttributeMap",
    "org.eknet.publet.web.util.Key",
    "org.eknet.publet.web.shiro.Security"
  )


  override def binder() = super.binder()

  /**
   * ServletContext Init-parameter.
   *
   * Comma or semicolon separated path of filenames or URLs
   * pointing to directories or jar files. Directories should end
   * with '/'.
   */
  val customClasspathInitParam = "custom-classpath"

  val servletContextName = Names.named("publetServletContext")

  override def configureServlets() {
    val eventBus = new EventBus("Publet Global EventBus")
    val config = new Config(servletContext.getContextPath, eventBus)

    bind[ServletContext]
      .annotatedWith(servletContextName)
      .toInstance(servletContext)

    bind[Config].toInstance(config)
    bind[EventBus].toInstance(eventBus)

    addInjectionListener(Matchers.any(), injectee => eventBus.register(injectee))

    eventBus.register(PubletWeb)
    bind[Settings].in(Scopes.SINGLETON)

    install(new AuthModule)
    install(new PubletShiroModule)

    val moduleManager = new ModuleManager(config)
    bind[ModuleManager].toInstance(moduleManager)

    moduleManager.modules foreach { m =>
      info("Installing module: %s".format(m.getClass.getName))
      install(m)
    }


    bind[DefaultLayout].asEagerSingleton()

    setOf[PartitionMount].add[FilesystemMounter].in(Scopes.SINGLETON)
    bind[PartitionMounter].asEagerSingleton()

    bindRequestHandler.add[PubletHandlerFactory]
    bindRequestHandler.add[AssetsHandlerFactory]

    bind[StartupScriptLoader].asEagerSingleton()

    filter("/*") through classOf[PubletMainFilter]
  }

  @Provides@com.google.inject.Singleton
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

  @Provides@com.google.inject.Singleton
  def createScalateEngine(publet: Publet, @Named("publetServletContext") servletContext: ServletContext, config: Config, assetMgr: AssetManager): ScalateEngine = {
    val e = new ConfiguredScalateEngine('wikiMain, publet, config, assetMgr)
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
    e.attributes = Map("includeLoader" -> new IncludeLoader(config, publet, assetMgr))
    publet.engineManager.register("/**", e)
    e
  }

  @Provides@com.google.inject.Singleton
  def createPubletCompiler(publet: Publet, @Named("publetServletContext") servletContext: ServletContext, config: Config): PubletCompiler = {
    val additionalImports = List(
      "org.eknet.publet.web.util.RenderUtils",
      "RenderUtils._"
    )
    new DefaultPubletCompiler(publet, config.mainMount,
      getCustomClasspath(servletContext), webImports ::: additionalImports)
  }

  @Provides@com.google.inject.Singleton@Named("ScriptEngine")
  def createScriptEngine(publet: Publet, scalateEngine: ScalateEngine, compiler: PubletCompiler): PubletEngine = {

    val scalaEngine = new ScalaScriptEngine('eval, compiler, scalateEngine)

    publet.engineManager.register("*.scala", scalaEngine)
    scalaEngine
  }

  @Provides@com.google.inject.Singleton
  def createAssetManager(publet: Publet, config: Config, bus: EventBus) = {
    val tempDir = config.newStaticTempDir("assets")
    val mgr: AssetManager = new DefaultAssetManager(publet, bus, tempDir)
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

object AppModule {

  val servletContextName = "publetServletContext"
  val servletContextAnnot = Names.named(servletContextName)

  val contentrootName = "contentroot"
  val contentrootAnnot = Names.named(contentrootName)
}