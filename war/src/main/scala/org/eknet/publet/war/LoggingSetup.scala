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
import org.eknet.publet.web.Config
import java.io.{FileInputStream, File}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 15.10.12 18:15
 */
class LoggingSetup extends ServletContextListener with LoggingConfigurer {

  def contextInitialized(sce: ServletContextEvent) {
    if (System.getProperty("logback.configurationFile") == null) {
      configureLogging(logfile(sce.getServletContext.getContextPath).map(f => new FileInputStream(f))
        .getOrElse(getClass.getResourceAsStream("/logback.xml")))
    }
  }

  def logfile(contextPath: String) = optFile(new File(Config.configDirectory(contextPath), "logback.xml"))
    .orElse(optFile(new File(Config.rootDirectory, "logback.xml")))
    .orElse(optFile(new File(new File("etc"), "logback.xml")))

  private def optFile(f: File): Option[File] = if (f.exists()) Some(f) else None

  def contextDestroyed(sce: ServletContextEvent) {
  }
}
