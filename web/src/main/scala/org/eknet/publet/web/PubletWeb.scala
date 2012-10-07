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

import guice.{Guicey, Names}
import javax.servlet.ServletContext
import shiro.UsersRealm
import org.eknet.publet.partition.git.{GitPartition, GitPartMan}
import org.eknet.publet.gitr.GitrMan
import org.eknet.publet.Publet
import org.eknet.publet.vfs.{Container, ContentResource, Path}
import util.{StringMap, PropertiesMap, AttributeMap, Key}
import org.apache.shiro.web.mgt.DefaultWebSecurityManager
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver
import org.apache.shiro.web.filter.authc.{AnonymousFilter, BasicHttpAuthenticationFilter, FormAuthenticationFilter}
import org.apache.shiro.web.env.{EnvironmentLoader, DefaultWebEnvironment}
import grizzled.slf4j.Logging
import org.eknet.publet.auth.{PubletAuth, RepositoryModel}
import org.apache.shiro.cache.MemoryConstrainedCacheManager
import ref.WeakReference
import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.engine.scalate.ScalateEngine
import com.google.inject
import inject.Injector

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 20:02
 */
object PubletWeb extends Guicey with Logging {

  /**
   * ServletContext Init-parameter.
   *
   * Comma or semicolon separated path of filenames or URLs
   * pointing to directories or jar files. Directories should end
   * with '/'.
   */
  val customClasspathInitParam = "custom-classpath"

  // initialized on context startup
  private var servletContextI: WeakReference[ServletContext] = null

  private val injectorKey = Key[Injector](classOf[Injector].getName)

  lazy val contextPath = servletContextI().getContextPath

  /**
   * Gets the Guice [[com.google.inject.Injector]] from the servlet context.
   *
   * @return
   */
  def injector = servletContextI.get.map { sc =>
    Option(sc.getAttribute(injectorKey.name))
      .getOrElse(sys.error("No Injector available in servletContext!")).asInstanceOf[Injector]
  } getOrElse(sys.error("No ServletContext has been set!"))

  def publet = instance[Publet]
  def scalateEngine = instance[ScalateEngine]
  def gitr: GitrMan = instance[GitrMan]
  def gitpartman: GitPartMan = instance[GitPartMan]
  def contentRoot = instance[Container](Names.contentroot)
  def authManager = instance[PubletAuth]
  def publetSettings = instance[StringMap](Names.settings)

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
   * Initializes the web app
   * @param sc
   */
  def initialize(sc: ServletContext) {
    this.servletContextI = new WeakReference[ServletContext](sc)

    publet.engineManager.register("/**", scalateEngine)
    val scriptEngine = instance[PubletEngine](Names.scriptEngine)
    publet.engineManager.register("*.scala", scriptEngine)

    WebExtensionLoader.onStartup()
  }


  def destroy(sc: ServletContext) {
    WebExtensionLoader.onShutdown()
    this.servletContextI.clear()
    this.servletContextI = null
  }
}
