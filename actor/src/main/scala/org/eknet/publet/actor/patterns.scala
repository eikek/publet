package org.eknet.publet.actor

import akka.util.Timeout
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 08.05.13 22:55
 */
object patterns {

  import scala.concurrent.duration._

  implicit def defaultTimeout: Timeout = 7.seconds

  object Stopwatch {
    def start(): () => Long = {
      val start = System.currentTimeMillis()
      () => System.currentTimeMillis() - start
    }
  }
}
