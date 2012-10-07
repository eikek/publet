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

import javax.servlet.{ServletContext, ServletContextEvent}
import org.eknet.publet.web.{WebExtensionLoader, Config, PubletWeb}
import java.io.File
import grizzled.slf4j.Logging
import org.eknet.publet.web.util.AppSignature
import com.google.inject.servlet.GuiceServletContextListener
import com.google.inject.{AbstractModule, Stage, Guice}
import org.eknet.publet.web.guice.{Names, PubletModule}
import collection.JavaConversions._
import ref.WeakReference

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 13:01
 */
class PubletContextListener extends GuiceServletContextListener with Logging with LoggingConfigurer {

  private var sc: WeakReference[ServletContext] = _

  object ServletContextModule extends AbstractModule {
    def configure() {
      bind(classOf[ServletContext])
        .annotatedWith(Names.servletContext)
        .toInstance(sc())
    }
  }
  override def contextInitialized(sce: ServletContextEvent) {
    //eagerly setting the servletContext. All eager injections must be
    //named with "Names.servletContext"
    this.sc = new WeakReference[ServletContext](sce.getServletContext)
    Config.setContextPath(sce.getServletContext.getContextPath)
    initLogging()

    super.contextInitialized(sce)
    info("""
           |
           |                   |      |        |
           |     __ \   |   |  __ \   |   _ \  __|
           |     |   |  |   |  |   |  |   __/  |
           |     .__/  \__,_| _.__/  _| \___| \__|
           |    _| $v$
           |
           |    starting ...
           |
           |""".stripMargin.replace("$v$", AppSignature.version))
    try {
      PubletWeb.initialize(sce.getServletContext)
      if (Config.mode == "development") {
        info("\n"+ ("-" * 75) + "\n !!! Publet is running in DEVELOPMENT Mode  !!!!\n" + ("-" * 75))
      }
      info(">>> publet initialized.\n")
    }
    catch {
      case e:Throwable => error("Error on startup!", e); throw e
    }
  }

  override def contextDestroyed(sce: ServletContextEvent) {
    super.contextDestroyed(sce)
    PubletWeb.destroy(sce.getServletContext)
  }

  private def stage = if (Config.mode == "development") Stage.DEVELOPMENT else Stage.PRODUCTION
  def getInjector = try {
    Guice.createInjector(stage, ServletContextModule :: PubletModule :: WebExtensionLoader.getModules)
  } catch {
    case e: Exception => error("Error creating guice module!", e); throw e
  }

  /**
   * Initializes logging from a "logback.xml" file in the directory
   * which is configured for publet. In standalone-mode this file
   * does not exist and the logging has already been initialised.
   *
   */
  private def initLogging() {
    var logfile = Config.getFile("logback.xml")
    if (!logfile.exists()) logfile = new File(Config.rootDirectory, "logback.xml")
    configureLogging(logfile)
  }
}
