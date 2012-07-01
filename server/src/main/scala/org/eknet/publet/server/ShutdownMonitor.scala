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

import actors.{DaemonActor, Actor}
import java.net.{Socket, InetAddress, ServerSocket}
import grizzled.slf4j.Logging
import java.io.{InputStreamReader, BufferedReader}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 01.07.12 01:36
 */
class ShutdownMonitor(val server: PubletServer, val port: Int) extends DaemonActor with Logging {

  val socket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"))

  def act() {
    info(">>> Starting shutdown monitor...")
    try {
      val acc = socket.accept()
      val reader = new BufferedReader(new InputStreamReader(acc.getInputStream))
      reader.readLine()
      info(">>> Shutting down publet server...")
      server.stop()
      server.server.setStopAtShutdown(false)
      acc.close()
      socket.close()
    } catch {
      case e: Exception => warn("Failed to shutdown jetty server!", e)
    }
  }

}
