package org.eknet.publet.engine

import org.eknet.publet.vfs._


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:05
 */
trait PubletEngine {

  def name: Symbol

  /**
   * Processes the given data input to an output format.
   *
   * @param data
   * @return
   */
  def process(path: Path, data: ContentResource, target: ContentType): Option[Content]

}
