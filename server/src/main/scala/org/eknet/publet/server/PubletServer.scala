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

package org.eknet.publet.server

import org.eclipse.jetty.server.{SslConnectionFactory, HttpConnectionFactory, ServerConnector, SecureRequestCustomizer, HttpConfiguration, Connector, Server}
import grizzled.slf4j.Logging
import java.io.File
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.util.ssl.SslContextFactory
import FileHelper._
import org.eclipse.jetty.util.component.LifeCycle

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.06.12 21:19
 */
class PubletServer(config: ServerConfig, setter: WebAppConfigurer) extends Logging {

  val varDir = {
    val varDir = file("var")
    if (!varDir.exists) {
      if (!varDir.mkdirs()) sys.error("Cannot create var directory: "+ varDir.getAbsolutePath)
    }
    varDir.ensuring(f => f.canWrite, "Cannot write to working dir: " + varDir.getAbsolutePath)
  }

  //properties to properly init the Config object
  System.setProperty("publet.dir", file("var").getAbsolutePath)
  System.setProperty("publet.standalone", "true")

  val server = {
    val server = new Server(new QueuedThreadPool(config.serverThreads))
    val httpconfig = new HttpConfiguration()
    httpconfig.setSendDateHeader(true)
    httpconfig.setSendServerVersion(false)

    server setStopTimeout (config.gracefulShutdownTimeout)
    server setStopAtShutdown true

    config.port map (port => { server addConnector (createConnector(server, httpconfig, port)) })
    config.securePort map (port => {
      val conn = createSslConnector(server, httpconfig, port, config.keystorePath, config.keystorePassword)
      server.addConnector(conn)
    })

    //configure the webapp
    setter.configure(server, config)
    server
  }


  def start() {
    startInBackground()
    server.join()
  }

  def startInBackground() {
    info(">>> Starting server ...")
    server.start()
  }

  def stop() {
    info(">>> Shutting down publet server ...")
    server.stop()
  }

  def addLifecycleListener(listener: LifeCycle.Listener) {
    server.addLifeCycleListener(listener)
  }

  def removeLifecycleListener(listener: LifeCycle.Listener) {
    server.removeLifeCycleListener(listener)
  }

  def createConnector(server: Server, httpc: HttpConfiguration, port: Int): Connector = {
    info(">>> Creating http connector for port "+ port+"; bind="+config.bindAddress)
    val sc = new ServerConnector(server, new HttpConnectionFactory(httpc))
    sc.setPort(port)

    sc.setSoLingerTime(-1)
    sc.setIdleTimeout(30000)
    config.bindAddress map (sc.setHost(_))

    sc
  }

  def createSslConnector(server: Server, httpc: HttpConfiguration, port: Int, keystorePath: String, password: String): Connector = {
    info(">>> Creating ssl connector for port "+ port + "; keystore="+keystorePath+"; bind="+config.sslBindAddress)
    val fac = new SslContextFactory
    val etcStore = file("etc"/"keystore.ks")
    if (keystorePath.isEmpty) {
      fac.setKeyStorePath(etcStore.getAbsolutePath)
      if (!etcStore.exists()) {
        info(">>> Generating self-signed certificate for localhost...")
        MakeCertificate.generateSelfSignedCertificate("localhost", etcStore, password)
      }
    } else {
      fac.setKeyStorePath(keystorePath)
    }
    fac.setKeyStorePassword(password)

    val https = new HttpConfiguration(httpc)
    https.addCustomizer(new SecureRequestCustomizer)

    val sc = new ServerConnector(server, new SslConnectionFactory(fac, "http/1.1"), new HttpConnectionFactory(https))
    sc.setPort(port)
    sc.setSoLingerTime(-1)
    sc.setIdleTimeout(30000)
    fac.setRenegotiationAllowed(true) //currently publet required java 1.7
    config.sslBindAddress map (sc.setHost(_))
    sc
  }

  private def file(path: FileHelper): File = (config.workingDirectory / path).asFile

}
