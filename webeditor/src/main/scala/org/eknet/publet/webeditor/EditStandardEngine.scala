package org.eknet.publet.webeditor

import org.eknet.publet.web.template.StandardEngine
import org.eknet.publet.Publet
import xml.{Null, Text, Attribute, NodeSeq}
import org.eknet.publet.vfs.{ContentType, NodeContent, Path}
import org.eknet.publet.web.{WebPublet, WebContext}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 19:10
 */
class EditStandardEngine(publet: Publet) extends StandardEngine(publet) {
  override def getSidebar(path: Path) = {
    Some(NodeContent(<div>Engine:
      <select class="publetRegisterEngine">
        {engineOptions}
      </select>
    </div>
      <h3>File browser</h3>
      <pre id="containerPath"></pre>
      <div id="filesTree"></div>, ContentType.html))
  }

  private def engineOptions: NodeSeq = {
    val publet = WebPublet().publet
    val current = publet.engineManager.resolveEngine(WebContext().requestPath)
    val opts = publet.engineManager.engines.keySet.filter(_ != 'edit).map(s => {
      val tag = <option>
        {s.name}
      </option>
      if (current.exists(_.name == s)) tag % Attribute("selected", Text("selected"), Null) else tag
    })
    NodeSeq.fromSeq(opts.toSeq)
  }
}
