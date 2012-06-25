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

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.05.12 20:30
 */
class PartitionMounter extends EmptyExtension with Logging {

  override def onStartup() {

    val publet = PubletWeb.publet

    // git partitions
    val partman = PubletWeb.gitpartman
    partman.getAllPartitions
      .collect({ case p if (p.getMountPoint.isDefined)=>p})
      .foreach(part => {
      info("Mounting repository '"+ part.tandem.name.name +"' to '"+ part.getMountPoint.get.asString+"'...")
      publet.mountManager.mount(part.getMountPoint.get, part)
    })

    // fs partitions
    val partCount = PartitionMounter.applyMounts("fs", (pdir, mountp) => {
      info("Mounting fs directory '"+ pdir+ "' to '"+ mountp.asString+"'")
      publet.mountManager.mount(mountp, new FilesystemPartition(pdir, true))
    })
    info("Mounted "+ partCount +" filesystem partition(s)")

    //webdav partitions (are fs partitions that are available via webdav)
    val webdavCount = PartitionMounter.applyMounts("webdav", (pdir, mountp) => {
      info("Mounting webdav directory '"+ pdir+ "' to '"+ mountp.asString +"'")
      publet.mountManager.mount(mountp, new FilesystemPartition(pdir, true))
    })
    info("Mounted "+ webdavCount +" webdav partitions")
  }
}

object PartitionMounter {

  private[web] def applyMounts(partType: String, f:(File, Path) => Unit): Int = {
    import Path._

    val fsKey = "partition."+partType+"."
    val settings = PubletWeb.publetSettings

    def mountFs(num:Int): Int = {
      val dir = settings(fsKey + num +".dir")
      val mount = settings(fsKey + num +".mount")
      if (dir.isDefined && mount.isDefined) {
        val pdir = new File(Config.configDirectory, dir.get)
        val mountp = mount.get.p

        f(pdir, mountp)

        1+ mountFs(num +1)
      } else {
        0
      }
    }
    mountFs(0)
  }
}
