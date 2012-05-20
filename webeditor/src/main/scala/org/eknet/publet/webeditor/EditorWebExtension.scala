package org.eknet.publet.webeditor

import actions.{BrowserJs, PushContents, ListContents, SetEngine}
import org.eknet.publet.Publet
import xml.NodeSeq
import org.eknet.publet.vfs.{Content, Path}
import org.eknet.publet.web.scripts.WebScriptResource
import org.eknet.publet.vfs.util.{MapContainer, ClasspathContainer}
import org.eknet.publet.web.{PubletWeb, WebExtension}
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 16:16
 */
class EditorWebExtension extends WebExtension with Logging {

  def onStartup() {
    info("Installing webeditor ...")
    EditorWebExtension.setup(PubletWeb.publet)
    PubletWeb.contextMap.put(PubletWeb.notFoundHandlerKey, new CreateNewHandler())
  }

  def onShutdown() {}
}

object EditorWebExtension {

  val editorPath = Path("/publet/webeditor/")
  val scriptPath = editorPath / "scripts"

  def setup(publet: Publet) {
    import org.eknet.publet.vfs.ResourceName._

    val cp = new ClasspathContainer(classOf[CreateNewHandler], None)
    publet.mountManager.mount(editorPath, cp)

    val muc = new MapContainer()
    muc.addResource(new WebScriptResource("setengine.json".rn, SetEngine))
    muc.addResource(new WebScriptResource("toc.json".rn, ListContents))
    muc.addResource(new WebScriptResource("push.json".rn, PushContents))
    muc.addResource(new WebScriptResource("browser.js".rn, BrowserJs))
    publet.mountManager.mount(scriptPath, muc)

    val se = new EditStandardEngine(publet)
//    publet.engineManager.addEngine(new EditEngine(se))
  }

  def headerHtml(path: Path, content: Content) = {
    val base = (Path(path.relativeRoot) / editorPath).asString
    //super.headerHtml(path, content, source) +
    NodeSeq.fromSeq(<link type="text/css" rel="stylesheet" href={base + "browser.css"}></link>
      <script src={base + "scripts/browser.js"}></script>).toString()
  }
}
