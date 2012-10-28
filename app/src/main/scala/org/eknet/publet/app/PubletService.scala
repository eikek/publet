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

import com.google.common.util.concurrent.AbstractService
import org.eknet.publet.server.{SyspropConfig, DefaultConfig, ServerConfig, PubletServer}
import org.eclipse.jetty.util.component.LifeCycle
import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener
import org.eclipse.jetty.webapp.{WebAppClassLoader, WebAppContext}
import org.eclipse.jetty.servlet.ServletContextHandler

/**
 * @param config configuration properties for the server
 * @param loader an optional class loader that is injected into jetty and used with the webapp
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.10.12 00:04
 */
class PubletService(config: ServerConfig, loader: Option[ClassLoader]) extends AbstractService {

  def this() = this(new DefaultConfig with SyspropConfig, None)

  private val server = new PubletServer(config, new CodeWebappConfigurer() {
    override protected def postProcessWebAppContext(webapp: WebAppContext) {
      processWebAppContext(webapp)
    }

    override protected def postProcessServletContext(sch: ServletContextHandler) {
      processServletContext(sch)
    }
  })

  server.addLifecycleListener(new AbstractLifeCycleListener() {
    override def lifeCycleFailure(event: LifeCycle, cause: Throwable) {
      notifyFailed(cause)
    }

    override def lifeCycleStarted(event: LifeCycle) {
      notifyStarted()
    }

    override def lifeCycleStopped(event: LifeCycle) {
      notifyStopped()
    }
  })

  def doStart() {
    server.startInBackground()
  }

  def doStop() {
    server.stop()
  }

  protected def processWebAppContext(webapp: WebAppContext) {
    loader.foreach( cl => {
      val wcl = new WebAppClassLoader(cl, webapp)
      webapp.setClassLoader(wcl)
    })
  }

  protected def processServletContext(sch: ServletContextHandler) {
    loader.foreach( cl => {
      sch.setClassLoader(cl)
    })
  }
}
