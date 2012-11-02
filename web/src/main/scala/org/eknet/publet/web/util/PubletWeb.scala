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

package org.eknet.publet.web.util

import javax.servlet.ServletContext
import org.eknet.publet.partition.git.{GitPartition, GitPartMan}
import org.eknet.publet.gitr.GitrMan
import org.eknet.publet.Publet
import org.eknet.publet.vfs.{Container, Path}
import grizzled.slf4j.Logging
import org.eknet.publet.auth.repository.{RepositoryTag, RepositoryModel}
import ref.WeakReference
import org.eknet.publet.engine.scalate.ScalateEngine
import com.google.inject
import inject.Injector
import com.google.common.eventbus.Subscribe
import org.eknet.publet.web.guice.{PubletShutdownEvent, PubletStartedEvent, Names, InjectorHelper}
import org.eknet.publet.auth.DefaultAuthStore

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 20:02
 */
object PubletWeb extends InjectorHelper with Logging {

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
  def authManager = instance[DefaultAuthStore]
  def publetSettings = instance[StringMap](Names.settings)

  /**
   * Returns the [[org.eknet.publet.auth.repository.RepositoryModel]] of the
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
      authManager.findRepository(name.name).getOrElse(RepositoryModel(name.name, RepositoryTag.open, ""))
    }
  }

  // ~~~ servlet context listener

  /**
   * Initializes the web app
   * @param sce
   */
  @Subscribe
  def initialize(sce: PubletStartedEvent) {
    this.servletContextI = new WeakReference[ServletContext](sce.sc)
  }


  @Subscribe
  def destroy(sce: PubletShutdownEvent) {
    this.servletContextI.clear()
    this.servletContextI = null
  }
}
