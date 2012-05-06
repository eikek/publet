package org.eknet.publet.partition.git

import org.eknet.publet.vfs.{Content, Path}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.05.12 16:36
 */
trait GitPartMan {

  def create(location: Path, config: Config): GitPartition

  def get(location: Path): Option[GitPartition]

  def getOrCreate(location: Path, config: Config): GitPartition
}

case class Config(initial: Option[Content], branch: String = "master")
