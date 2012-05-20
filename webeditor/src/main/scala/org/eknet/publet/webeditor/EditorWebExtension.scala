package org.eknet.publet.webeditor

import actions.{BrowserJs, PushContents, ListContents, SetEngine}
import org.eknet.publet.Publet
import org.eknet.publet.web.scripts.WebScriptResource
import org.eknet.publet.vfs.util.{MapContainer, ClasspathContainer}
import org.eknet.publet.web.{PubletWeb, WebExtension}
import grizzled.slf4j.Logging
import org.eknet.publet.vfs.Path
import org.eknet.publet.engine.scalate.ScalateEngine

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 16:16
 */
class EditorWebExtension extends WebExtension with Logging {

  def onStartup() {
    info("Installing webeditor ...")
    EditorWebExtension.setup(PubletWeb.publet, PubletWeb.scalateEngine)
    PubletWeb.contextMap.put(PubletWeb.notFoundHandlerKey, new CreateNewHandler())
  }

  def onShutdown() {}
}

object EditorWebExtension {

  val editorPath = Path("/publet/webeditor/")
  val scriptPath = editorPath / "scripts"

  def setup(publet: Publet, scalateEngine: ScalateEngine) {
    import org.eknet.publet.vfs.ResourceName._

    val cp = new ClasspathContainer(classOf[CreateNewHandler], Some(Path("includes/")))
    publet.mountManager.mount(editorPath, cp)

    val muc = new MapContainer()
//    muc.addResource(new WebScriptResource("setengine.json".rn, SetEngine))
    muc.addResource(new WebScriptResource("toc.json".rn, ListContents))
    muc.addResource(new WebScriptResource("push.json".rn, PushContents))
    muc.addResource(new WebScriptResource("browser.js".rn, BrowserJs))
    publet.mountManager.mount(scriptPath, muc)

    val editEngine = new WebEditor('edit, scalateEngine)
    publet.engineManager.addEngine(editEngine)
  }

}
