package org.eknet.publet.ext

import org.eknet.publet.Publet
import org.eknet.publet.vfs.util.MapContainer
import org.eknet.publet.web.scripts.WebScriptResource
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.{PubletWeb, WebExtension}
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 20:17
 */
class ExtWebExtension extends WebExtension with Logging {

  def onStartup() {
    info("Installing publet extensions...")
    ExtWebExtension.install(PubletWeb.publet)
  }

  def onShutdown() {
  }
}

object ExtWebExtension {

  val extScriptPath = Path("/publet/ext/scripts/")

  def install(publet: Publet) {
    import org.eknet.publet.vfs.ResourceName._
    val muc = new MapContainer()
    muc.addResource(new WebScriptResource("captcha.png".rn, CaptchaScript))
    muc.addResource(new WebScriptResource("mailcontact.html".rn, MailContact))
    muc.addResource(new WebScriptResource("listing.html".rn, Listing))
    publet.mountManager.mount(extScriptPath, muc)
  }
}
