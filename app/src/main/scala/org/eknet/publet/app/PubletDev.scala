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

import org.eknet.publet.server.{SyspropConfig, DefaultConfig}
import scala.io.Source
import java.util.concurrent.TimeUnit
import com.google.common.util.concurrent.Service.State
import java.net.{MalformedURLException, URL, URLClassLoader}
import java.io.File
import com.google.common.base.Splitter

/**
 * Simple main class to startup publet, intended to use from
 * within an IDE. Config settings can be overriden using system
 * properties. See [[org.eknet.publet.server.ServerConfig]]
 * companion object for property names.
 *
 * The thread waits for input on stdin and if committed, the server will shutdown.
 *
 * The system property `publet.ext.classpath` can be used to specify a list of
 * files, directories or urls that make up an additoinal classpath. Each item must
 * be separated by colon. This classpath is appended when starting publet. This
 * can be used to load extensions.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 12.11.12 18:37
 */
object PubletDev extends App {

  val classPath = "publet.ext.classpath"
  val classPathSeparator = ':'

  val server = {
    val config = new DefaultConfig with SyspropConfig
    config.setWorkingDirectory("target/publetwork")
    config.setPort(Some(8081))
    new PubletService(config, createClassLoader)
  }

  private[this] def createClassLoader = {
    import collection.JavaConversions._
    Option(System.getProperty(classPath)) match {
      case Some(value) if (!value.isEmpty) => {
        val strList = Splitter.on(classPathSeparator).trimResults().omitEmptyStrings().split(value).toList
        val urls = strList.map(cp => new File(cp) match {
          case f if (f.exists()) => f.toURI.toURL
          case _ => new URL(cp)
        })
        Some(new URLClassLoader(urls.toArray, Thread.currentThread().getContextClassLoader))
      }
      case _ => None
    }
  }

  server.start().get(30, TimeUnit.SECONDS)
  println("\n>> Press ENTER to stop")
  Source.stdin.getLines().toStream(0)
  val state = server.stop().get(30, TimeUnit.SECONDS)
  val rc = if (state == State.TERMINATED) 0 else 1
  System.exit(rc)

}
