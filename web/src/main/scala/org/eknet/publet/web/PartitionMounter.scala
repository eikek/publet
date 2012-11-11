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

package org.eknet.publet.web

import _root_.com.google.common.eventbus.{EventBus, Subscribe}
import _root_.com.google.inject.{Inject, Singleton}
import grizzled.slf4j.Logging
import guice.PubletStartedEvent
import org.eknet.publet.vfs.Path
import org.eknet.publet.vfs.fs.FilesystemPartition
import java.io.File
import util.StringMap
import org.eknet.publet.Publet

/**
 * Checks the `Config` and/or `Settings` file for listed partitions to mount.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.05.12 20:30
 */
@Singleton
class PartitionMounter @Inject() (config: Config, settings: Settings) extends Logging {
  import collection.JavaConversions._

  private var _mounter: java.util.Set[PartitionMount] = Set[PartitionMount]()

  @Inject(optional = true)
  def setMounters(mounter: java.util.Set[PartitionMount]) {
    this._mounter = mounter
  }

  @Subscribe
  def mountPartitions(ev: PubletStartedEvent) {

    def mount(cfg: PartitionConfig): Int = {
      _mounter.foldLeft(0)((i, pm) => i + (if (pm.isDefinedAt(cfg)) pm(cfg) else 0))
    }

    val configs = getPartitionConfigs(config, settings)
    val count = if (configs.isEmpty) 0 else configs.map(mount).reduceLeft(_ + _)
    info("Mounted "+ count +" partition(s)")
  }

  /**
   * Extracts the partition configuration from a properties file. The configuration
   * has the following structure:
   *
   * {{{
   *   partition.0.type=fs
   *   partition.0.dir=parts/files
   *   partition.0.mounts=/files , /dav/files
   *
   *   partition.1.type=gitr
   *   partition.1.dir=wiki/eike
   *   partition.1.mounts=/wikis/eike  /dav/wikis/eike
   *
   *   partition.2.type=fs
   *   partition.2.dir=artifacts/maven2
   *   partition.2.mounts=/maven2
   * }}}
   *
   * A list of mount points can be specified by separating the path entries
   * by comma, semi-colon or whitespace.
   *
   * @param config
   * @return
   */
  private def readPartitionConfig(config: StringMap): List[PartitionConfig] = {

    def recurseRead(num: Int): List[PartitionConfig] = {

      def key(name:String) = "partition."+num+"."+name

      config(key("type")) match {
        case Some(kind) => {
          val mounts = config(key("mounts")) match {
            case None => List[Path]()
            case Some(s) => s.split("[\\s,;]").withFilter(_.nonEmpty).map(Path(_)).toList
          }
          val cdir = config(key("dir"))
          if (cdir.getOrElse("").isEmpty) {
            error("A mount configuration has been provided without configuring the directory! See '"+ key("dir")+"' key.")
            recurseRead(num +1)
          } else if (mounts.isEmpty) {
            error("A mount configuration has been provided without a mount point! See '"+ key("mounts")+"' key.")
            recurseRead(num +1)
          } else {
            val part = PartitionConfig(kind, cdir.get, mounts)
            part :: recurseRead(num +1)
          }
        }
        case None => Nil
      }
    }

    recurseRead(0)
  }

  /** by default partitions are read from settings. this can be overridden
    * in the config file so that all definitions from settings are ignored
    * @param config
    * @param settings
    * @return
    */
  private def getPartitionConfigs(config: Config, settings: Settings) =
    if (config("applyPartitionSettings").map(_.toBoolean).getOrElse(true)) {
      readPartitionConfig(settings)
    } else {
      readPartitionConfig(config)
    }
}

case class PartitionConfig(kind: String, directory: String, mounts: List[Path])

trait PartitionMount extends PartialFunction[PartitionConfig, Int]

abstract class PartitionTypeMount(types: String*) extends PartitionMount {
  def isDefinedAt(x: PartitionConfig) = x match {
    case PartitionConfig(kind, _, _) if (types.toSet.contains(kind)) => true
    case _ => false
  }
}

@Singleton
class FilesystemMounter @Inject() (publet: Publet, bus: EventBus) extends PartitionTypeMount("fs") with Logging {

  def apply(cfg: PartitionConfig) = {
    info("Mounting fs directory '"+ cfg.directory+ "' to '"+ cfg.mounts.map(_.asString)+"'")
    val pdir = new File(Config.get.configDirectory, cfg.directory)
    val fsp = new FilesystemPartition(pdir, bus, true)
    for (m <- cfg.mounts) publet.mountManager.mount(m, fsp)
    1
  }

}
