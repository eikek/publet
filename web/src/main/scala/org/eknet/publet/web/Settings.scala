package org.eknet.publet.web

import util.PropertiesMap
import WebContext._
import org.eknet.publet.vfs.{Path, ContentResource}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 05:22
 */
object Settings extends PropertiesMap {

  reload()

  def file = {
    val publet = WebContext().service(webPubletKey).publet
    publet.rootContainer.lookup(Path("/.allIncludes/settings.properties")) match {
      case cr: ContentResource if (cr.exists) => Some(cr.inputStream)
      case _ => None
    }
  }
}
