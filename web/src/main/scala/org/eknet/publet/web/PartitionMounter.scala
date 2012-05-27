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

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.05.12 20:30
 */
class PartitionMounter extends WebExtension with Logging {

  def onStartup() {
    val publet = PubletWeb.publet
    val partman = PubletWeb.gitpartman
    partman.getAllPartitions
      .collect({ case p if (p.getMountPoint.isDefined)=>p})
      .foreach(part => {
      info("Mounting repository '"+ part.tandem.name.name +"' to '"+ part.getMountPoint.get.asString+"'...")
      publet.mountManager.mount(part.getMountPoint.get, part)
    })
  }

  def onShutdown() {}
}
