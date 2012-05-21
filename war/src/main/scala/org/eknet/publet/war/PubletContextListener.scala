package org.eknet.publet.war

import scala.collection.JavaConversions._
import javax.servlet.{ServletContextEvent, ServletContextListener}
import org.eknet.publet.webeditor.EditorWebExtension
import org.eknet.publet.web.template.BootstrapTemplate
import org.eknet.publet.doc.PubletDocExtension
import org.eknet.publet.web.{Config, WebExtension, PubletWeb}
import java.io.File
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.util.StatusPrinter
import java.util.ServiceLoader


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 13:01
 */
class PubletContextListener extends ServletContextListener {

  def loadExtensions(): List[WebExtension] = {
    val loader = ServiceLoader.load(classOf[WebExtension])
    loader.iterator().toList
  }

  def contextInitialized(sce: ServletContextEvent) {
    PubletWeb.initialize(sce.getServletContext)
    initLogging()
    loadExtensions().foreach(_.onStartup())
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
