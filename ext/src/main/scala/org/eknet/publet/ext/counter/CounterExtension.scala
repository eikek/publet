package org.eknet.publet.ext.counter

import org.eknet.publet.web.{PubletWebContext, WebExtension}
import grizzled.slf4j.Logging
import javax.servlet.http.HttpServletRequest
import com.google.common.eventbus.Subscribe
import org.eknet.publet.web.guice.{PubletShutdownEvent, PubletStartedEvent}
import com.google.inject.Singleton

/**
 * Installs the [[org.eknet.publet.ext.counter.CounterService]] and a thread that
 * is collecting access information for resources.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.06.12 21:49
 */
@Singleton
class CounterExtension extends WebExtension with Logging {

  private val counterThread = new SaveCountActor

  @Subscribe
  def onStartup(ev: PubletStartedEvent) {
    counterThread.start()
  }

  @Subscribe
  def onShutdown(ev: PubletShutdownEvent) {
    counterThread ! StopMessage
  }


  def onBeginRequest(req: HttpServletRequest) = req

  def onEndRequest(req: HttpServletRequest) {
    if (PubletWebContext.getErrorResponse.isEmpty) {
      val uri = CounterExtension.getDefaultCountingUri
      val cinfo = PubletWebContext.getClientInfo
      counterThread ! Message(uri, cinfo)
    }
  }
}

object CounterExtension {

  def getDefaultCountingUri =
    PubletWebContext.applicationUri + PubletWebContext.getQueryString.map("?"+_).getOrElse("")
}
