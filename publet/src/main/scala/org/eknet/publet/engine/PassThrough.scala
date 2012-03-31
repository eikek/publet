package org.eknet.publet.engine

import org.eknet.publet.{Path, ContentType, Content}


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 23:38
 */
object PassThrough extends PubletEngine {

  def name = 'source

  def process(path: Path, data: Seq[Content], target: ContentType) = Right(data.head)

}
