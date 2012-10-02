package org.eknet.publet.ext.counter

import org.eknet.publet.web.{PubletWeb, PubletWebContext, WebExtension}
import org.eknet.publet.web.util.Key
import java.net.{UnknownHostException, InetAddress}
import grizzled.slf4j.Logging
import javax.servlet.http.HttpServletRequest

/**
 * Installs the [[org.eknet.publet.ext.counter.CounterService]] and a thread that
 * is collecting access information for resources.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.06.12 21:49
 */
class CounterExtension extends WebExtension with Logging {
  import CounterExtension._

  private val counterThread = new SaveCountActor

  def onStartup() {
    val service = CounterService()
    PubletWeb.contextMap.put(serviceKey, service)
    counterThread.start()

    val ipRegex = """\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}""".r
    val keyPrefix = "ext.counter.blacklist."
    for (key <- PubletWeb.publetSettings.keySet if (key.startsWith(keyPrefix))) {
      key.substring(keyPrefix.length) match {
        case ipRegex() =>
        case hostname => {
          try {
            val ip = InetAddress.getByName(hostname).getHostAddress
            info("Resolved hostname '" + hostname + "'. Add '" + ip + "' to counter blacklist...")
            PubletWeb.publetSettings.put("ext.counter.blacklist." + ip, PubletWeb.publetSettings(key).get)
          }
          catch {
            case e:UnknownHostException => error("Cannot resolve hostname '"+hostname+"'! Cannot add to counter blacklist.")
          }
        }
      }
    }
  }

  def onShutdown() {
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

  private val serviceKey = Key[CounterService]("counterServiceKey")

  /**
   * Returns a instance of [[org.eknet.publet.ext.counter.CounterService]] that
   * is cached in context scope. Use this in web environment.
   *
   * @return
   */
  def service = PubletWeb.contextMap.get(serviceKey).get

  def serviceOption = PubletWeb.contextMap.get(serviceKey)

  def getDefaultCountingUri =
    PubletWebContext.applicationUri + PubletWebContext.getQueryString.map("?"+_).getOrElse("")
}
