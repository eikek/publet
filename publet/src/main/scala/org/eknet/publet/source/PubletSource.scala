package org.eknet.publet.source

import org.eknet.publet.{Uri, Data, Named}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:05
 */
trait PubletSource extends Named {

  /**
   * Looks up the resource as specified by the uri
   *
   * @param uri
   * @return
   */
  def lookup(uri: Uri): Option[Data]

  def push(uri: Uri)(data: Data)

  def pushFunction(uri:Uri) = push(uri)_
}
