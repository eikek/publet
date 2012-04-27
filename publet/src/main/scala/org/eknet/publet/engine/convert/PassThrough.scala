package org.eknet.publet.engine.convert

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.vfs.{ContentResource, ContentType}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 23:38
 */
object PassThrough extends PubletEngine {

  def name = 'source

  def process(data: Seq[ContentResource], target: ContentType) = Some(data.head)

}
