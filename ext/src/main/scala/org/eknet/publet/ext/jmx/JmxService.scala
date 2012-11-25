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

package org.eknet.publet.ext.jmx

import grizzled.slf4j.Logging
import java.lang.management.ManagementFactory
import javax.management.remote.{JMXConnectorServerFactory, JMXConnectorServer, JMXServiceURL}
import org.eknet.publet.web.Config
import com.google.inject.{Inject, Singleton}
import com.google.common.util.concurrent.{AbstractService, Service}
import java.util
import org.eknet.publet.web.guice.{PubletShutdownEvent, PubletStartedEvent}
import com.google.common.eventbus.Subscribe
import java.util.Hashtable
import javax.management.ObjectName

/**
 * @author Eike Kettner eike.kettner@gmail.comy
 * @since 24.11.12 15:27
 */
@Singleton
class JmxService @Inject() (config: Config) extends AbstractService with Logging {

  private[this] var server: JMXConnectorServer = null

  @Subscribe
  def autoStart(event: PubletStartedEvent) {
    start()
  }

  @Subscribe
  def autoStop(event: PubletShutdownEvent) {
    stopAndWait()
  }

  def doStart() {
    val jmxPort = config("publet.jmx.port").map(_.toInt)
    val jmxHost = config("publet.jmx.host")
    (jmxHost, jmxPort) match {
      case (host, Some(port)) => {
        try {
          val url = new JMXServiceURL("jmxmp", host.orNull, port)
          val env = new util.HashMap[String, Object]()
          if (config("publet.jmx.protected").map(_.toBoolean).getOrElse(false)) {
            val authc = new JmxAuthenticator
            env.put(JMXConnectorServer.AUTHENTICATOR, authc)
          }
          this.server = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mBeanServer)
          server.start()
          info("Started JMX Connector at '"+ url +"'")
          notifyStarted()
        } catch {
          case e: Exception => {
            error("Error starting JMX connector", e)
            notifyFailed(e)
          }
        }
      }
      case _ => {
        info("Not starting JMX Connector. No port given in config file.")
        notifyStarted()
      }
    }
  }

  def doStop() {
    try {
      Option(server) map { ms =>
        ms.stop()
        info("Stopped JMX connector.")
      }
      this.server = null
      notifyStopped()
    }
    catch {
      case e: Exception => {
        error("Error stopping JMX connector", e)
        notifyFailed(e)
      }
    }
  }

  def mBeanServer = {
    ManagementFactory.getPlatformMBeanServer
  }
}

object JmxService {

  def defaultMBeanServer = ManagementFactory.getPlatformMBeanServer

  def registerMBean(mbean: AnyRef, createName: AnyRef => ObjectName = createObjectName) {
    defaultMBeanServer.registerMBean(mbean, createObjectName(mbean))
  }

  def createObjectName(inst: AnyRef) = {
    val fcn = inst.getClass.getName
    val scn = inst.getClass.getSimpleName
    val domain = fcn.substring(0, fcn.length - scn.length -1)

    val table = new Hashtable[String, String]()
    table.put("type", scn)
    table.put("name", scn)
    ObjectName.getInstance(domain, table)
  }
}