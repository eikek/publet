package org.eknet.publet.web

import javax.servlet.ServletContext

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 20:31
 */
trait WebExtension {

  def onStartup(publet: WebPublet, sc: ServletContext)

}
