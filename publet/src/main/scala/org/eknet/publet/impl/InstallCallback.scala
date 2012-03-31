package org.eknet.publet.impl

import org.eknet.publet.Publet

/**
 * An engine can implement this interface and the method is invoked once the
 * engine is added.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 31.03.12 16:16
 */
trait InstallCallback {

  def onInstall(publ: Publet) {}

}
