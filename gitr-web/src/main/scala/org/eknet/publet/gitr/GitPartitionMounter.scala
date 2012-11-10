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

package org.eknet.publet.gitr

import org.eknet.publet.web.guice.PubletStartedEvent
import com.google.inject.{Singleton, Inject}
import org.eknet.publet.Publet
import org.eknet.publet.web.{PartitionConfig, PartitionMounter, Settings, Config}
import com.google.common.eventbus.Subscribe
import grizzled.slf4j.Logging
import org.eknet.publet.vfs.Path
import org.eknet.publet.gitr.partition.GitPartMan

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.11.12 18:13
 */
@Singleton
class GitPartitionMounter @Inject() (publet: Publet, partman: GitPartMan, config: Config, settings: Settings) extends Logging {

  @Subscribe
  def mountGitPartitions(event: PubletStartedEvent) {

    val configs = PartitionMounter.configs(config, settings)

    def mount(cfg: PartitionConfig): Int = cfg match {
      case PartitionConfig("git", dir, mounts) => {
        info("Mounting git repository '"+dir+"' to '"+ mounts.map(_.asString)+"'")
        val gitp = partman.getOrCreate(Path(dir), partition.Config())
        for (m <- mounts) publet.mountManager.mount(m, gitp)
        1
      }
      case PartitionConfig(kind, _, _) => {
        0
      }
    }

    val count = if (configs.isEmpty) 0 else configs.map(mount).reduceLeft(_ + _)
    info("Mounted "+ count +" partition(s)")
  }
}
