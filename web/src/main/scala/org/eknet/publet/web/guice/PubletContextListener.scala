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

package org.eknet.publet.web.guice

import javax.servlet.{ServletContext, ServletContextEvent}
import org.eknet.publet.web.{RunMode, Config}
import grizzled.slf4j.Logging
import org.eknet.publet.web.util.{PubletWeb, AppSignature}
import com.google.inject.servlet.GuiceServletContextListener
import com.google.inject._
import ref.WeakReference
import com.google.common.eventbus.EventBus
import org.eknet.publet.event.Event

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 13:01
 */
class PubletContextListener extends GuiceServletContextListener with Logging {

  private var sc: WeakReference[ServletContext] = _

  override def contextInitialized(sce: ServletContextEvent) {
    //eagerly setting the servletContext. All eager injections must be
    //named with "Names.servletContext"
    this.sc = new WeakReference[ServletContext](sce.getServletContext)
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
    super.contextInitialized(sce)
    val bus = findInjector.getInstance(classOf[EventBus])
    val config = findInjector.getInstance(classOf[Config])

    try {
      if (config.mode == RunMode.development) {
        info("\n"+ ("-" * 75) + "\n !!! Publet is running in DEVELOPMENT Mode  !!!!\n" + ("-" * 75))
      }
      val event = new PubletStartedEvent(sce.getServletContext)
      PubletWeb.initialize(event)
      bus.post(event)
      info(">>> publet initialized.\n")
    }
    catch {
      case e:Throwable => error("Error on startup!", e); throw e
    }
  }

  override def contextDestroyed(sce: ServletContextEvent) {
    val bus = findInjector.getInstance(classOf[EventBus])
    val event = new PubletShutdownEvent(sce.getServletContext)
    bus.post(event)
    PubletWeb.destroy(event)
    super.contextDestroyed(sce)
    this.sc.clear()
  }

  def findInjector = sc().getAttribute(classOf[Injector].getName).asInstanceOf[Injector]

  def getInjector = try {
    Guice.createInjector(Stage.PRODUCTION, new AppModule(sc()))
  } catch {
    case e: Exception => error("Error creating guice module!", e); throw e
  }

}

case class PubletStartedEvent(sc: ServletContext) extends Event
case class PubletShutdownEvent(sc:ServletContext) extends Event