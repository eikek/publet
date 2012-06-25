package org.eknet.publet.web.util

import java.util
import java.text.SimpleDateFormat

/**
 * Gives build information about publet.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 25.06.12 19:16
 *
 */
object AppSignature {
  import org.eknet.publet.reflect.Reflect

  /** The application name, which is "publet" */
  val name = Reflect.name

  /** The version of this app */
  val version = Reflect.version

  /** The build time of this version */
  val timestamp = new util.Date(Reflect.timestamp)

  /** The build time formatted according to ISO8601 */
  val timeString = {
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    df.setTimeZone(util.TimeZone.getTimeZone("GMT"))
    df.format(timestamp)
  }

  override def toString =  {
    val sig = name +" "+ version
    if (version.endsWith("SNAPSHOT")) sig +" ["+ timeString +"]"
    else sig
  }

}
