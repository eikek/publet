package org.eknet.publet.ext.counter

import actors.DaemonActor
import java.util.concurrent.TimeUnit
import org.eknet.publet.web.util.ClientInfo
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.06.12 00:03
 */
private[counter] class SaveCountActor extends DaemonActor with Logging {

  private val cache = collection.mutable.Map[String, Long]()

  private val timeWindow = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)
  private val maxCacheSize = 500

  def act() {
    loop({
      react({
        case StopMessage => {
          exit()
        }
        case Message(uri, ci) => {
          cache.get(key(uri, ci)) match {
            case Some(time) => {
              val now = System.currentTimeMillis()
              if ((now - time) > timeWindow) {
                cache.put(key(uri, ci), now)
                countAccess(uri, ci)
              }
            }
            case None => {
              cache.put(key(uri, ci), System.currentTimeMillis())
              countAccess(uri, ci)
              if (cache.size > maxCacheSize) cacheCleanup()
            }
          }
        }
      })
    })
  }

  private def key(uri: String, info: ClientInfo) = info.ip +":"+ uri

  private def cacheCleanup() {
    val now = System.currentTimeMillis()
    for ((key, value) <- cache.clone()) {
      if ((now - value) > timeWindow) cache.remove(key)
    }
    while (cache.size > maxCacheSize) {
      cache.drop(0)
    }
  }

  private def countAccess(uri:String, info: ClientInfo) {
    try {
      CounterService().collect(uri, info)
    }
    catch {
      case e: Exception => error("Exception while counting!", e)
    }
  }
}

private[counter] case class Message(uri: String, info: ClientInfo)

private[counter] case object StopMessage