/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  def setExportOk(location: Path, flag: Boolean): Boolean

  def isExportOk(location: Path): Boolean

  def getAllPartitions: Iterable[GitPartition]
}

case class Config(initial: Option[Map[Path, Content]] = None, branch: String = "master", mountPoint: Option[Path] = None)
