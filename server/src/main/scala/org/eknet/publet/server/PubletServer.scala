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
import org.eclipse.jetty.webapp.WebAppContext
import actors.Actor
import grizzled.slf4j.Logging
import java.io.{FileFilter, File}
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.util.ssl.SslContextFactory
import sbt.IO

/**
 * Works for the following directory structure
 * {{{
 *   /etc - configuration files
 *   /bin - start/stop scripts and the publet-server.jar
 *   /var - data directory
 *   /webapp - the war file
 * }}}
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.06.12 21:19
 */
class PubletServer(config: ServerConfig) extends Logging with LoggingConfigurer {

  val tempDir = new File(new File("temp"), "jetty")
  if (!tempDir.exists()) tempDir.mkdirs()

  //configure logging
  new File(new File("etc"), "logback.xml") match {
    case f if (f.exists()) => {
      configureLogging(f)
      info("Configured logging from file: "+ f)
    }
    case _ =>
  }

  System.setProperty("publet.dir", "var")

  val server = new Server

  server setGracefulShutdown (8000)
  server setSendServerVersion false
  server setSendDateHeader true
  server setStopAtShutdown true

  config.port map (port => {
    val connector = createConnector(port)
    server addConnector (connector)
  })

  config.securePort map (port => {
    val conn = createSslConnector(port, config.keystorePath, config.keystorePassword)
    server.addConnector(conn)
  })


  val webapp = new WebAppContext
  webapp setContextPath ("/")
  webapp.setServer(server)
  webapp.setTempDirectory(new File(new File("temp"), "jetty"))
  webapp setWar("webapp")
  entries(new File("plugins"), f => f !=null && f.isFile).map(_.getAbsolutePath) match {
    case list if (!list.isEmpty) => webapp.setExtraClasspath(list.mkString(";"))
    case _ =>
  }

  server.setHandler(webapp)

  def start() {
    info(">>> Starting server ...")
    server.start()
    server.join()
  }

  def stop() {
    info(">>> Shutting down publet server ...")
    server.stop()
  }


  def createConnector(port: Int): Connector = {
    info(">>> Creating http connector for port "+ port)
    val conn = new SelectChannelConnector
    conn.setSoLingerTime(-1)
    conn.setThreadPool(new QueuedThreadPool(20))
    conn.setPort(port)
    conn.setMaxIdleTime(30000)
    conn
  }

  def createSslConnector(port: Int, keystorePath: String, password: String): Connector = {
    info(">>> Creating ssl connector for port "+ port + "; keystore="+keystorePath)
    val fac = new SslContextFactory
    fac.setKeyStorePath(keystorePath)
    fac.setKeyStorePassword(password)
    fac.setAllowRenegotiate(true) //currently publet required java 1.7

    val conn = new SslSelectChannelConnector(fac)
    conn.setSoLingerTime(-1)
    conn.setThreadPool(new QueuedThreadPool(20))
    conn.setMaxIdleTime(30000)
    conn.setPort(port)
    conn
  }


  implicit def fun2Filter(f:File => Boolean): FileFilter = new FileFilter {
    def accept(pathname: File) = f(pathname)
  }

  def entries(f: File, filter:File=>Boolean):List[File] = f :: (if (f.isDirectory) listFiles(f, filter).toList.flatMap(entries(_, filter)) else Nil)
  private def listFiles(f:File, filter:File=>Boolean): Array[File] = f.listFiles(filter) match {
    case null => Array[File]()
    case o@_ => o
  }
}
