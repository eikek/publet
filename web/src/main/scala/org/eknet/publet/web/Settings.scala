package org.eknet.publet.web

import util.PropertiesMap
import WebContext._
import org.eknet.publet.vfs.{Path, ContentResource}
import org.eknet.publet.{Publet, Includes}
import java.io.{FileInputStream, File}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 05:22
 */
object Settings extends PropertiesMap {

  reload()

  def file = {
    val settingsFile = new File(Config.contentRoot, Includes.allIncludes + "settings.properties")
    if (settingsFile.exists()) Some(new FileInputStream(settingsFile))
    else None
  }
}
