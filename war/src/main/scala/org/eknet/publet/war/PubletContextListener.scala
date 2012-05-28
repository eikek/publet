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

package org.eknet.publet.war

import javax.servlet.{ServletContextEvent, ServletContextListener}
import org.eknet.publet.web.{Config, WebExtension, PubletWeb}
import java.io.File
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.util.StatusPrinter
import java.util.ServiceLoader
import grizzled.slf4j.Logging


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 13:01
 */
class PubletContextListener extends ServletContextListener with Logging {

  def contextInitialized(sce: ServletContextEvent) {
    info("""
           |                   |      |        |
           |     __ \   |   |  __ \   |   _ \  __|
           |     |   |  |   |  |   |  |   __/  |
           |     .__/  \__,_| _.__/  _| \___| \__|
           |    _|
           |
           |    starting ...
           |
           |""".stripMargin)
    try {
      PubletWeb.initialize(sce.getServletContext, initLogging)
      info(">>> publet initialized.\n")
    }
    catch {
      case e:Throwable => error("Error on startup!", e); throw e
    }
  }

  def contextDestroyed(sce: ServletContextEvent) {
    PubletWeb.destroy(sce.getServletContext)
  }

  private def initLogging() {
    var logfile = Config.getFile("logback.xml")
    if (!logfile.exists()) logfile = new File(Config.rootDirectory, "logback.xml")
    if (logfile.exists()) {
      println("Configuring logging...")
      val context = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
      try {
        val configurer = new JoranConfigurator
        configurer.setContext(context)

        context.reset()
        configurer.doConfigure(logfile)
      } catch {
        case e: JoranException =>
      }
      StatusPrinter.printInCaseOfErrorsOrWarnings(context)
    }
  }
}
