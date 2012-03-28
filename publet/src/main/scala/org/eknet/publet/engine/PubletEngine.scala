package org.eknet.publet.engine

import org.eknet.publet.{Data, Named}


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:05
 */
trait PubletEngine extends Named {

  def process(data: Data): Either[Exception, Data]

}
