package org.eknet.publet.webeditor

import actions.{PushContents, ListContents, SetEngine}
import org.eknet.publet.Publet
import org.eknet.publet.vfs.virtual.{ClasspathContainer, MutableContainer}
import xml.NodeSeq
import org.slf4j.LoggerFactory
import org.eknet.publet.vfs.{ContentType, Content, Path}
import org.eknet.publet.web.scripts.WebScriptResource
import javax.servlet.ServletContext
import org.eknet.publet.web.{WebContext, WebPublet, WebExtension}

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
    val cp = new ClasspathContainer(editorPath, classOf[CreateNewHandler], None)
    publet.mountManager.mount(editorPath, cp)

    val muc = new MutableContainer(editorPath/"scripts")
    muc.addResource(new WebScriptResource(scriptPath / "setengine.json", SetEngine, ContentType.json))
    muc.addResource(new WebScriptResource(scriptPath / "toc.json", ListContents, ContentType.json))
    muc.addResource(new WebScriptResource(scriptPath / "push.json", PushContents, ContentType.json))
    publet.mountManager.mount(editorPath/"scripts", muc)

    val se = new EditStandardEngine(publet)
    se.htmlHeadContribs.append(headerHtml)
    publet.engineManager.addEngine(new EditEngine(se))
  }

  def headerHtml(path: Path, content: Content) = {
    val base = (Path(path.relativeRoot) / editorPath).asString
    //super.headerHtml(path, content, source) +
    NodeSeq.fromSeq(<link type="text/css" rel="stylesheet" href={base + "browser.css"}></link>
      <script src={base + "browser.js"}></script>).toString()
  }
}
