package org.eknet.publet.source

import org.eknet.publet.Path


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 30.03.12 20:56
 */
class RootPartition extends Partition with MountManager {

  def name = 'root

  def lookup(path: Path) = partitionAt(path).flatMap(_.lookup(path))

}
