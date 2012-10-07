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

import com.google.inject.{Singleton, Provides, AbstractModule}
import org.eknet.publet.web.template.{IncludeLoader, ConfiguredScalateEngine}
import org.eknet.publet.engine.scala.{ScalaScriptEngine, DefaultPubletCompiler, ScriptCompiler}
import java.io.File
import java.net.URL
import tools.nsc.util.ScalaClassLoader.URLClassLoader
import org.fusesource.scalate.Binding
import org.eknet.publet.Publet
import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.vfs.{ContentResource, Container, ResourceName, Path}
import org.eknet.publet.web.{PubletWeb, Config}
import org.eknet.publet.vfs.util.MapContainer
import org.eknet.publet.web.scripts.{Logout, Login, WebScriptResource}
import org.eknet.publet.partition.git.{GitPartManImpl, GitPartMan, GitPartition}
import org.eknet.publet.gitr.{GitrMan, GitrManImpl}
import org.eknet.publet.auth.PubletAuth
import org.eknet.publet.web.shiro.AuthManager
import javax.servlet.ServletContext
import com.google.inject.servlet.ServletModule
import com.google.inject.name.Named
import org.eknet.publet.engine.scalate.ScalateEngine
import org.eknet.publet.web.asset.impl.DefaultAssetManager
import org.eknet.publet.web.asset.AssetManager
import org.eknet.publet.web.util.{StringMap, PropertiesMap}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.10.12 23:19
 */
object PubletModule extends ServletModule {

  private val webImports = List(
    "org.eknet.publet.web.Config",
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
    install(PubletShiroModule)
  }

  @Provides@Singleton
  def createAuthManager: PubletAuth = new AuthManager

  @Provides@Singleton
  def createGitrManager: GitrMan = new GitrManImpl(Config.repositories)

  @Provides@Singleton
  def createGitPartitionManager(gitr: GitrMan): GitPartMan = new GitPartManImpl(gitr)

  @Provides@Singleton@Named("contentroot")
  def createMainPartition(gitr: GitPartMan): Container =
    gitr.getOrCreate(contentRootRepo, org.eknet.publet.partition.git.Config(None))

  @Provides@Singleton
  def createPublet(@Named("contentroot") mainPartition: Container) = {
    val publ = Publet()
    publ.mountManager.mount(Path(Config.mainMount).toAbsolute, mainPartition)

    //scripts
    val cont = new MapContainer()
    cont.addResource(new WebScriptResource(ResourceName("login.json"), Login))
    cont.addResource(new WebScriptResource(ResourceName("logout.json"), Logout))
    publ.mountManager.mount(Path("/publet/scripts/"), cont)

    publ
  }

  @Provides@Singleton
  def createScalateEngine(publet: Publet, servletContext: ServletContext): ScalateEngine = {
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
  def createScriptEngine(publet: Publet, scalateEngine: ScalateEngine, servletContext: ServletContext): PubletEngine = {
    val additionalImports = List(
      "org.eknet.publet.web.util.RenderUtils",
      "RenderUtils._"
    )
    val compiler = new DefaultPubletCompiler(publet, Config.mainMount,
      getCustomClasspath(servletContext), webImports ::: additionalImports)
    val scalaEngine = new ScalaScriptEngine('eval, compiler, scalateEngine)

    scalaEngine
  }

  @Provides@Singleton
  def createAssetManager(publet: Publet) = {
    val tempDir = Config.newStaticTempDir("assets")
    val mgr: AssetManager = new DefaultAssetManager(publet, tempDir)
    mgr
  }

  @Provides@Singleton@Named("settings")
  def createSettings(@Named("contentroot") contentRoot: Container): StringMap = {
    new PropertiesMap {
      reload()
      override def file = contentRoot.lookup(Path(Publet.allIncludes+"config/settings.properties"))
        .collect({case cc: ContentResource => cc})
        .map(_.inputStream)
    }
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