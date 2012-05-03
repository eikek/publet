package org.eknet.publet.web

import util.PropertiesMap
import WebContext._
import org.eknet.publet.vfs.{Path, ContentResource}
import org.eknet.publet.{Publet, Includes}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 05:22
 */
object Settings extends PropertiesMap {

  reload()

  private def allIncludesFile(publet: Publet, filename: String) = publet.rootContainer
    .lookup((Path(Config.mainMount)/ Includes.allIncludesPath /filename).toAbsolute)

  def file = {
    val publet = WebPublet().publet
    allIncludesFile(publet, "settings.properties") match {
      case Some(cr: ContentResource) if (cr.exists) => Some(cr.inputStream)
      case _ => None
    }
  }
}
