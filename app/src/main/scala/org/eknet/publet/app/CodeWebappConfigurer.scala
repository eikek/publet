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
import org.eknet.publet.war.PubletContextListener
import org.eknet.publet.web.filter.RoutingFilter

/**
 * Configures the jetty server programmatically. No web.xml is needed, use this
 * to start the server from code.
 */
object CodeWebappConfigurer extends WebAppConfigurer {
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
    sessionManager.setSecureRequestOnly(config.securePort.isDefined && config.ajpPort.isEmpty && config.port.isEmpty)
    webapp.getSessionHandler.setSessionManager(sessionManager)

    //the web.xml
    val sch = new ServletContextHandler(server, "/", ServletContextHandler.NO_SECURITY)
    sch.setBaseResource(baseResource)
    sch.setSessionHandler(new SessionHandler())
    sch.addFilter(classOf[RoutingFilter], "/*", util.EnumSet.of(DispatcherType.REQUEST))
    sch.addEventListener(new PubletContextListener)
    val nullServletHolder = new ServletHolder()
    nullServletHolder.setServlet(new HttpServlet {})
    sch.addServlet(nullServletHolder, "/")

    val coll = new ContextHandlerCollection()
    coll.addHandler(sch)
    coll.addHandler(webapp)
    server.setHandler(coll)
  }

}
