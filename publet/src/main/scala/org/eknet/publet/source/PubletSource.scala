package org.eknet.publet.source

import java.net.URI
import org.eknet.publet.{Data, Named}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:05
 */
trait PubletSource extends Named {

  def lookup(uri: URI): Option[Data]

  def push(uri: URI)(data: Data)

  def pushFunction(uri:URI) = push(uri)_
}
