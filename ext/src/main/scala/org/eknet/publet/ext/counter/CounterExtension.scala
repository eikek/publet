package org.eknet.publet.ext.counter

import org.eknet.publet.web.{PubletWebContext, WebExtension}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.06.12 21:49
 */
class CounterExtension extends WebExtension{

  def onStartup() {
    CounterDb.withTx ( db => {
      val x = CounterDb.referenceNode
      println(x.getId)
    })
  }

  def onShutdown() {
    CounterDb.db.shutdown()
  }

  def onBeginRequest() {
    val uri = PubletWebContext.requestUri

  }

  def onEndRequest() {}
}
