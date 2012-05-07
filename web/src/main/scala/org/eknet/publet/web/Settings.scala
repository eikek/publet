package org.eknet.publet.web

import util.PropertiesMap
import java.io.InputStream

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 05:22
 */
class Settings(override val file: Option[InputStream]) extends PropertiesMap {

  reload()

}
