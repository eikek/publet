package org.eknet.publet.web

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 20:31
 */
trait WebExtension {

  /**
   * Point on which extension code is executed
   * once per server start.
   *
   * It is ensured, that those are invoked
   * _after_ [[org.eknet.publet.web.PubletWeb]]
   * has been initialized.
   */
  def onStartup()


  def onShutdown()

}
