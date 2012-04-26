package org.eknet.publet.ext

import org.eknet.publet.Publet
import org.eknet.publet.vfs.virtual.MutableContainer
import org.eknet.publet.web.scripts.WebScriptResource
import org.eknet.publet.vfs.{ContentType, Path}
import org.slf4j.LoggerFactory
import org.eknet.publet.web.{WebPublet, WebExtension, WebContext}
import javax.servlet.{ServletContext, ServletContextEvent, ServletContextListener}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 20:17
 */
class ExtWebExtension extends WebExtension {

  private val log = LoggerFactory.getLogger(getClass)


  def onStartup(publet: WebPublet, sc: ServletContext) {
    log.info("Installing publet extensions...")
    ExtWebExtension.install(publet.publet)
  }

}

object ExtWebExtension {

  val extPath = Path("/publet/ext/")
  val extScriptPath = extPath / "scripts/"

  def install(publet: Publet) {
    val muc = new MutableContainer(extScriptPath)
    muc.addResource(new WebScriptResource(extScriptPath / "captcha.png", CaptchaScript, ContentType.png))
    muc.addResource(new WebScriptResource(extScriptPath / "mailcontact.html", MailContact, ContentType.html))
    publet.mountManager.mount(extScriptPath, muc)
  }
}
