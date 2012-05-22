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
