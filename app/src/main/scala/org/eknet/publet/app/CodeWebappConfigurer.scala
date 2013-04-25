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

package org.eknet.publet.app

import org.eclipse.jetty.server.Server
import org.eknet.publet.server.{WebAppConfigurer, FileHelper, ServerConfig}
import java.io.File
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.server.session.{SessionHandler, HashSessionManager}
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import java.util
import javax.servlet.DispatcherType
import javax.servlet.http.HttpServlet
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eknet.publet.web.req.PubletMainFilter
import org.eknet.publet.web.guice.PubletContextListener
import com.google.inject.servlet.GuiceFilter
import org.eknet.publet.war.LoggingSetup

/**
 * Configures the jetty server programmatically. No web.xml is needed, use this
 * to start the server from code.
 *
 * The directory containing the extension/plugin jar files can be specified with
 * the constructor. If not specified, the `plugin` directory in the working directory
 * is tried.
 *
 */
class CodeWebappConfigurer(pluginDir: Option[File]) extends WebAppConfigurer {

  def this() = this(None)

  def configure(server: Server, config: ServerConfig) {
    import FileHelper._
    def file(path: FileHelper): File = (config.workingDirectory / path).asFile

    val webapp = new WebAppContext
    webapp.setServer(server)
    webapp setContextPath (config.contextPath)
    val tempdir = file("temp" / "jetty")
    if (!tempdir.exists()) {
      tempdir.mkdirs()
    }
    val baseResource = Resource.newResource(file("webapp"))
    webapp.setTempDirectory(tempdir)
    webapp.setBaseResource(baseResource)
    val sessionManager = new HashSessionManager()
    sessionManager.setHttpOnly(true)
    sessionManager.setSecureRequestOnly(config.securePort.isDefined && config.port.isEmpty)
    webapp.getSessionHandler.setSessionManager(sessionManager)
    pluginDir.orElse(file("plugins").asFileIfPresent).foreach(dir => {
      entries(dir, f => f !=null && f.isFile).map(_.getAbsolutePath) match {
        case list if (!list.isEmpty) => webapp.setExtraClasspath(list.mkString(";"))
        case _ =>
      }
    })
    postProcessWebAppContext(webapp)

    //the web.xml
    val sch = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS)
    sch.setBaseResource(baseResource)
    sch.setSessionHandler(new SessionHandler())
    sch.addFilter(classOf[GuiceFilter], "/*", util.EnumSet.of(DispatcherType.REQUEST))
    sch.addEventListener(new LoggingSetup)
    sch.addEventListener(new PubletContextListener)
    val nullServletHolder = new ServletHolder()
    nullServletHolder.setServlet(new HttpServlet {})
    sch.addServlet(nullServletHolder, "/")
    postProcessServletContext(sch)

    val coll = new ContextHandlerCollection()
    coll.addHandler(sch)
    coll.addHandler(webapp)
    server.setHandler(coll)
  }

  protected def postProcessWebAppContext(webapp: WebAppContext) {
  }

  protected def postProcessServletContext(sch: ServletContextHandler) {
  }
}
