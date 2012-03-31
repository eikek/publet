package org.eknet.publet.source

import org.eknet.publet.{Path, Content, Named}


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:05
 */
trait Partition extends Named {

  /**
   * Looks up the resource as specified by the path
   *
   * @param path
   * @return
   */
  def lookup(path: Path): Option[Content]

}
