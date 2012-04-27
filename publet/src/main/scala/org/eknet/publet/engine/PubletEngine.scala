package org.eknet.publet.engine

import org.eknet.publet.Named
import org.eknet.publet.vfs._


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:05
 */
trait PubletEngine extends Named {

  /**
   * Processes one of the given data inputs to an output format.
   * There must be at least one element in the Seq
   * <p>
   * The input are different format variations of the same content.
   * </p>
   *
   * @param data
   * @return
   */
  def process(data: Seq[ContentResource], target: ContentType): Option[Content]

  def processToResource(data: Seq[ContentResource], target: ContentType): Option[ContentResource] = {
    process(data, target) map { content =>
      new PathContentResource(data.head.path.withExt(target.extensions.head), data.head.parent, content)
    }
  }
}
