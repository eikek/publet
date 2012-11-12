package org.eknet.publet.ext.graphdb

import org.eknet.publet.web.EmptyExtension
import com.google.inject.{Inject, Singleton}
import org.eknet.publet.Publet
import com.google.common.eventbus.Subscribe
import org.eknet.publet.web.guice.{PubletShutdownEvent, PubletStartedEvent}
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.06.12 16:51
 */
@Singleton
class GraphDbShutdownHook @Inject() (dbprov: GraphDbProvider) extends Logging {

  @Subscribe
  def onShutdown(ev: PubletShutdownEvent) {
    info("Shutting down graph databases...")
    dbprov.shutdownAll()
  }
}
