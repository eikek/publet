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
   * Processes one of the given data inputs to an output format.
   * There must be at least one element in the Seq
   * <p>
   * The input are different format variations of the same content.
   * </p>
   *
   * @param data
   * @return
   */
  def process(path: Path, data: Seq[ContentResource], target: ContentType): Option[Content]

}
