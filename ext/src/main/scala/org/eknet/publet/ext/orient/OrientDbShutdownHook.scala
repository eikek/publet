package org.eknet.publet.ext.orient

import org.eknet.publet.web.EmptyExtension
import com.orientechnologies.orient.core.Orient
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
class OrientDbShutdownHook extends Logging {

  @Subscribe
  def onShutdown(ev: PubletShutdownEvent) {
    //this really shuts down all orient databases!
    //normally, this is called within a jvm shutdown hook. but
    //that would prevent reloading webapps without server restart
    info("Shutting down orientdb databases...")
    Orient.instance.shutdown()
  }
}
