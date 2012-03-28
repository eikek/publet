package org.eknet.publet.engine

import org.eknet.publet.Data

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 23:38
 */
class PassThrough extends PubletEngine {

  def name = 'passthrough

  def process(data: Data) = Right(data)

}
