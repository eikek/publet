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
import org.eknet.publet.web.template.{DefaultLayout, IncludeLoader, ConfiguredScalateEngine}
import org.eknet.publet.engine.scala.{ScalaScriptEngine, DefaultPubletCompiler, ScriptCompiler}
import java.io.File
import java.net.URL
import tools.nsc.util.ScalaClassLoader.URLClassLoader
import org.fusesource.scalate.Binding
import org.eknet.publet.Publet
import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.vfs.{Container, ResourceName, Path}
import org.eknet.publet.web.{PartitionMounter, Settings, Config}
import org.eknet.publet.vfs.util.MapContainer
import org.eknet.publet.web.scripts.{Logout, Login, WebScriptResource}
import javax.servlet.ServletContext
import com.google.inject.servlet.ServletModule
import com.google.inject.name.Named
import org.eknet.publet.engine.scalate.ScalateEngine
import org.eknet.publet.web.asset.impl.DefaultAssetManager
import org.eknet.publet.web.asset.AssetManager
import com.google.common.eventbus.EventBus
import com.google.inject.matcher.Matchers
import com.google.inject.spi.{InjectionListener, TypeEncounter, TypeListener}
import org.eknet.publet.web.util.{PubletWeb, StringMap}
import org.eknet.publet.web.req._
import grizzled.slf4j.Logging
import org.eknet.guice.squire.SquireBinder
import org.eknet.publet.auth.guice.AuthModule

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.10.12 23:19
 */
class AppModule(servletContext: ServletContext) extends ServletModule with PubletBinding with Logging with SquireBinder {

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


  override def configureServlets() {
    val eventBus = new EventBus("Publet Global EventBus")
    val config = new Config(servletContext.getContextPath, eventBus)

    bind[ServletContext]
      .annotatedWith(Names.servletContext)
      .toInstance(servletContext)

    bind[Config].toInstance(config)
    bind[EventBus].toInstance(eventBus)

    bindListener(Matchers.any(), new TypeListener {
      def hear[I](`type`: TypeLiteral[I], encounter: TypeEncounter[I]) {
        encounter.register(new InjectionListener[I] {
          def afterInjection(injectee: I) {
            eventBus.register(injectee)
          }
        })
      }
    })
    eventBus.register(PubletWeb)
    bind[Settings].in(Scopes.SINGLETON)

    install(new AuthModule)
    install(PubletShiroModule)

    val moduleManager = new ModuleManager(config)
    bind[ModuleManager].toInstance(moduleManager)

    moduleManager.modules foreach { m =>
      info("Installing module: %s".format(m.getClass.getName))
      install(m)
    }


    bind[DefaultLayout].asEagerSingleton()
    bind[PartitionMounter].asEagerSingleton()

    bindRequestHandler.add[PubletHandlerFactory]
    bindRequestHandler.add[AssetsHandlerFactory]

    filter("/*") through classOf[PubletMainFilter]
  }

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

  @Provides@Singleton@Named("ScriptEngine")
  def createScriptEngine(publet: Publet, scalateEngine: ScalateEngine, @Named("publetServletContext") servletContext: ServletContext, config: Config): PubletEngine = {
    val additionalImports = List(
      "org.eknet.publet.web.util.RenderUtils",
      "RenderUtils._"
    )
    val compiler = new DefaultPubletCompiler(publet, config.mainMount,
      getCustomClasspath(servletContext), webImports ::: additionalImports)
    val scalaEngine = new ScalaScriptEngine('eval, compiler, scalateEngine)

    publet.engineManager.register("*.scala", scalaEngine)
    scalaEngine
  }

  @Provides@Singleton
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