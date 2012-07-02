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

import grizzled.slf4j.Logging
import java.net.{InetAddress, Socket}
import java.io.{FileInputStream, File}
import java.util

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 01.07.12 01:30
 */
object Main extends App with Logging {

  val etc = new File("etc")

  lazy val config = new DefaultConfig with SyspropConfig with PropertiesConfig {
    val props = new util.Properties

    new File(etc, "server.properties") match {
      case f if (f.exists()) => props.load(new FileInputStream(f))
      case _ =>
    }
  }

  if (args.isEmpty) error("Specify --start or --stop")
  else if (args(0) == "--start") startup()
  else if (args(0) == "--stop") shutdown()
  else error("Unknown: "+ args.mkString(" "))


  def startup() {
    val varDir = new File("var")
    if (!varDir.exists) {
      if (!varDir.mkdirs()) sys.error("Cannot create var directory: "+ varDir.getAbsolutePath)
    }
    varDir.ensuring(f => f.canWrite, "Cannot write to working dir: " + new File("").getAbsolutePath)
    etc.ensuring(f => f.exists() && f.isDirectory, "Cannot find `etc` directory: "+ etc)
    new File("webapp").ensuring(f => f.exists && f.isDirectory, "Cannot find `webapp` directory")

    val service = new PubletServer(config)
    val monitor = new ShutdownMonitor(service, config.shutdownPort)
    monitor.start()
    service.start()
  }

  def shutdown() {
    val s = new Socket(InetAddress.getByName("127.0.0.1"), config.shutdownPort)
    val out = s.getOutputStream
    info(">> Sending shutdown request to server ...")
    out.write(("\r\n").getBytes)
    out.flush()
    s.close()
  }
}
