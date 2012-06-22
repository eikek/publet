package org.eknet.publet.ext.counter

import org.eknet.publet.web.{PubletWeb, PubletWebContext, WebExtension}
import com.tinkerpop.blueprints.Vertex
import org.eknet.publet.web.util.Key
import com.orientechnologies.orient.core.Orient
import org.eknet.publet.vfs.util.{UrlResource, MapContainer}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.06.12 21:49
 */
class CounterExtension extends WebExtension{
  import CounterExtension._

  private val counterThread = new SaveCountActor

  def onStartup() {
    val service = CounterService()
    PubletWeb.contextMap.put(serviceKey, service)
    counterThread.start()
  }

  def onShutdown() {
    service.shutdown()
    counterThread ! StopMessage
    //this really shuts down the database(s)
    //normally, this is called within a jvm shutdown hook. but that
    //prevents reloading webapps without server restart
    Orient.instance.shutdown()
  }

  def onBeginRequest() {
  }

  def onEndRequest() {
    val uri = PubletWebContext.fullUrl
    val cinfo = PubletWebContext.getClientInfo
    counterThread ! Message(uri, cinfo)
  }
}

object CounterExtension {

  private val serviceKey = Key[CounterService]("counterServiceKey")

  /**
   * Returns a instance of [[org.eknet.publet.ext.counter.CounterService]] that
   * is cached in context scope. Use this in web environment.
   *
   * @return
   */
  def service = PubletWeb.contextMap.get(serviceKey).get

}
