package org.eknet.publet.webeditor

import org.eknet.publet.Publet
import xml.{Null, Text, Attribute, NodeSeq}
import org.eknet.publet.vfs.Path
import org.eknet.publet.engine.scala.ScalaScript._
import org.eknet.publet.web.{PubletWebContext, PubletWeb}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 19:10
 */
class EditStandardEngine(publet: Publet) {
  def getSidebar(path: Path) = {
    makeSsp {
      <div>Engine:
        <select class="publetRegisterEngine">
          {engineOptions}
        </select>
      </div>
      <h3>File browser</h3>
      <pre id="containerPath"></pre>
      <div id="filesTree"></div>
    }
  }

  private def engineOptions: NodeSeq = {
    val publet = PubletWeb.publet
    val current = publet.engineManager.resolveEngine(PubletWebContext.applicationPath)
    val opts = publet.engineManager.engines.keySet.filter(_ != 'edit).map(s => {
      val tag = <option>
        {s.name}
      </option>
      if (current.exists(_.name == s)) tag % Attribute("selected", Text("selected"), Null) else tag
    })
    NodeSeq.fromSeq(opts.toSeq)
  }
}
