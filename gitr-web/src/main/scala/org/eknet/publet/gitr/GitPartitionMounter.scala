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
import org.eknet.publet.web._
import com.google.common.eventbus.Subscribe
import grizzled.slf4j.Logging
import org.eknet.publet.vfs.Path
import org.eknet.publet.gitr.partition.GitPartMan
import org.eknet.publet.web.guice.PubletStartedEvent
import org.eknet.publet.web.PartitionConfig

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.11.12 18:13
 */
@Singleton
class GitPartitionMounter @Inject() (publet: Publet, partman: GitPartMan) extends PartitionTypeMount("git") with Logging {

  def apply(cfg: PartitionConfig) = {
    info("Mounting git repository '"+cfg.directory+"' to '"+ cfg.mounts.map(_.asString)+"'")
    val gitp = partman.getOrCreate(Path(cfg.directory), partition.Config())
    for (m <- cfg.mounts) publet.mountManager.mount(m, gitp)
    1
  }

}
