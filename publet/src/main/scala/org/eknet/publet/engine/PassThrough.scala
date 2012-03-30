package org.eknet.publet.engine

import org.eknet.publet.{ContentType, Page}


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 23:38
 */
class PassThrough extends PubletEngine {

  def name = 'source

  def process(data: Seq[Page], target: ContentType) = Right(data.head)

}
