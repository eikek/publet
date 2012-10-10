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

import grizzled.slf4j.Logging
import org.eknet.publet.vfs.Path
import org.eknet.publet.vfs.fs.FilesystemPartition
import java.io.File
import util.{StringMap, PropertiesMap}
import org.eknet.publet.partition.git

/**
 * Checks the `Config` and/or `Settings` file for listed partitions to mount.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.05.12 20:30
 */
class PartitionMounter extends EmptyExtension with Logging {

  override def onStartup() {

    val publet = PubletWeb.publet
    //by default partitions are read from settings. this can be overridden
    //in the config file so that all definitions from settings are ignored
    val configs = if (Config("applyPartitionSettings").map(_.toBoolean).getOrElse(true)) {
      readPartitionConfig(PubletWeb.publetSettings)
    } else {
      readPartitionConfig(Config.get)
    }

    def mount(cfg: PartitionConfig): Int = cfg match {
      case PartitionConfig("fs", dir, mounts) => {
        info("Mounting fs directory '"+ dir+ "' to '"+ mounts.map(_.asString)+"'")
        val pdir = new File(Config.get.configDirectory, dir)
        val fsp = new FilesystemPartition(pdir, true)
        for (m <- mounts) publet.mountManager.mount(m, fsp)
        1
      }
      case PartitionConfig("git", dir, mounts) => {
        info("Mounting git repository '"+dir+"' to '"+ mounts.map(_.asString)+"'")
        val gitp = PubletWeb.gitpartman.getOrCreate(Path(dir), git.Config())
        for (m <- mounts) publet.mountManager.mount(m, gitp)
        1
      }
      case PartitionConfig(kind, _, _) => {
        error("Uknown partition type '"+ kind +"' !")
        0
      }
    }

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
  def readPartitionConfig(config: StringMap): List[PartitionConfig] = {

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

  case class PartitionConfig(kind: String, directory: String, mounts: List[Path])
}
