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

import org.eclipse.jetty.webapp.WebAppContext
import java.io.{FileFilter, File}
import org.eclipse.jetty.server.session.HashSessionManager
import org.eclipse.jetty.server.Server

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.09.12 00:01
 */
trait WebAppConfigurer {

  def configure(server: Server, config: ServerConfig)

}

/**
 * Sets up the jetty server when running from the extracted zip file.
 *
 * Works for the following directory structure
 * {{{
 *   /etc - configuration files
 *   /bin - start/stop scripts and the publet-server.jar
 *   /var - data directory
 *   /webapp - the war file
 * }}}
 *
 */
object ZipFileConfigurer extends WebAppConfigurer {
  def configure(server: Server, config: ServerConfig) {
    import FileHelper._
    def file(path: FileHelper): File = (config.workingDirectory / path).asFile

    file("webapp").ensuring(f => f.exists && f.isDirectory, "Cannot find `webapp` directory")

    val webapp = new WebAppContext
    webapp.setServer(server)
    webapp setContextPath (config.contextPath)
    val tempdir = file("temp" / "jetty")
    if (!tempdir.exists()) {
      tempdir.mkdirs()
    }
    webapp.setTempDirectory(tempdir)
    webapp setWar(file("webapp").getAbsolutePath)
    entries(file("plugins"), f => f !=null && f.isFile).map(_.getAbsolutePath) match {
      case list if (!list.isEmpty) => webapp.setExtraClasspath(list.mkString(";"))
      case _ =>
    }
    val sessionManager = new HashSessionManager()
    sessionManager.setHttpOnly(true)
    sessionManager.setSecureRequestOnly(config.securePort.isDefined && config.ajpPort.isEmpty && config.port.isEmpty)
    webapp.getSessionHandler.setSessionManager(sessionManager)

    server.setHandler(webapp)
  }

  def entries(f: File, filter:File=>Boolean):List[File] = f :: (if (f.isDirectory) listFiles(f, filter).toList.flatMap(entries(_, filter)) else Nil)
  private def listFiles(f:File, filter:File=>Boolean): Array[File] = f.listFiles(filter) match {
    case null => Array[File]()
    case o@_ => o
  }

  implicit def fun2Filter(f:File => Boolean): FileFilter = new FileFilter {
    def accept(pathname: File) = f(pathname)
  }

}

