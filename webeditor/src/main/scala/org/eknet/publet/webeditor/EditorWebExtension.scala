package org.eknet.publet.webeditor

import actions.{BrowserJs, PushContents, ListContents, SetEngine}
import org.eknet.publet.Publet
import xml.NodeSeq
import org.slf4j.LoggerFactory
import org.eknet.publet.vfs.{Content, Path}
import org.eknet.publet.web.scripts.WebScriptResource
import javax.servlet.ServletContext
import org.eknet.publet.web.{WebContext, WebPublet, WebExtension}
import org.eknet.publet.vfs.util.{MapContainer, ClasspathContainer}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 16:16
 */
class EditorWebExtension extends WebExtension {

  private val log = LoggerFactory.getLogger(classOf[EditorWebExtension])


  def onStartup(publet: WebPublet, sc: ServletContext) {
    log.info("Registering webeditor ...")
    EditorWebExtension.setup(publet.publet)
    sc.setAttribute(WebContext.notFoundHandlerKey.name, new CreateNewHandler())
  }

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
    se.htmlHeadContribs.append(headerHtml)
    publet.engineManager.addEngine(new EditEngine(se))
  }

  def headerHtml(path: Path, content: Content) = {
    val base = (Path(path.relativeRoot) / editorPath).asString
    //super.headerHtml(path, content, source) +
    NodeSeq.fromSeq(<link type="text/css" rel="stylesheet" href={base + "browser.css"}></link>
      <script src={base + "scripts/browser.js"}></script>).toString()
  }
}
