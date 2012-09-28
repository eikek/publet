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

import org.eclipse.jetty.server.{Connector, Server}
import org.eclipse.jetty.server.nio.SelectChannelConnector
import grizzled.slf4j.Logging
import java.io.File
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.eclipse.jetty.ajp.Ajp13SocketConnector
import FileHelper._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.06.12 21:19
 */
class PubletServer(config: ServerConfig, setter: WebAppConfigurer) extends Logging with LoggingConfigurer {

  //configure logging
  file("etc" / "logback.xml") match {
    case f if (f.exists()) => {
      configureLogging(f)
      info("Configured logging from file: "+ f)
    }
    case _ => file("logback.xml") match {
      case f if (f.exists()) => {
        configureLogging(f)
        info("Configured logging from file: "+ f)
      }
      case _ =>
    }
  }

  val varDir = file("var")
  if (!varDir.exists) {
    if (!varDir.mkdirs()) sys.error("Cannot create var directory: "+ varDir.getAbsolutePath)
  }
  varDir.ensuring(f => f.canWrite, "Cannot write to working dir: " + varDir.getAbsolutePath)

  //properties to properly init the Config object
  System.setProperty("publet.dir", file("var").getAbsolutePath)
  System.setProperty("publet.standalone", "true")

  val server = new Server
  server setGracefulShutdown (8000)
  server setSendServerVersion false
  server setSendDateHeader true
  server setStopAtShutdown true

  config.port map (port => { server addConnector (createConnector(port)) })
  config.securePort map (port => {
    val conn = createSslConnector(port, config.keystorePath, config.keystorePassword)
    server.addConnector(conn)
  })
  config.ajpPort map (port => server.addConnector(createAjpConnector(port)))

  //configure the webapp
  setter.configure(server, config)

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

  def createConnector(port: Int): Connector = {
    info(">>> Creating http connector for port "+ port+"; bind="+config.bindAddress)
    val conn = new SelectChannelConnector
    conn.setSoLingerTime(-1)
    conn.setThreadPool(new QueuedThreadPool(20))
    conn.setPort(port)
    conn.setMaxIdleTime(30000)
    config.bindAddress map (conn.setHost(_))
    conn
  }

  def createSslConnector(port: Int, keystorePath: String, password: String): Connector = {
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
    fac.setAllowRenegotiate(true) //currently publet required java 1.7

    val conn = new SslSelectChannelConnector(fac)
    conn.setSoLingerTime(-1)
    conn.setThreadPool(new QueuedThreadPool(20))
    conn.setMaxIdleTime(30000)
    conn.setPort(port)
    config.sslBindAddress map (conn.setHost(_))
    conn
  }

  def createAjpConnector(port: Int): Connector = {
    info(">>> Creating AJP connector for port: "+ port)
    val conn = new Ajp13SocketConnector
    conn.setThreadPool(new QueuedThreadPool(20))
    conn.setPort(port)
    conn
  }


  private def file(path: FileHelper): File = (config.workingDirectory / path).asFile

}
