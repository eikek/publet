package org.eknet.publet.web.template

import java.util.UUID
import org.eknet.publet.resource.Partition._
import org.eknet.publet.impl.InstallCallback
import org.eknet.publet.{Publet, Path}
import org.eknet.publet.resource.Content
import org.eknet.publet.web.WebContext
import xml.{Null, Text, Attribute, NodeSeq}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 07.04.12 21:28
 */

trait FilebrowserTemplate extends Yaml2ColTemplate with InstallCallback {

  private val randPath = UUID.randomUUID().toString

  val editPartition = classpath(Path("../themes/browser"), classOf[FilebrowserTemplate])


  override def onInstall(publ: Publet) {
    super.onInstall(publ)
    publ.mount(Path("/"+randPath+"/browser"), editPartition)
  }

  override def headerHtml(path: Path, content: Content, source: Seq[Content]) = {
    val base = path.relativeRoot + randPath + "/"
    super.headerHtml(path, content, source) +
     NodeSeq.fromSeq(<link type="text/css" rel="stylesheet" href={ base + "browser/browser.css" }></link>
     <script src={ base +"browser/jquery-1.7.2.min.js" } ></script>
     <script src={ base +"browser/browser.js" } ></script>).toString()
  }

  def yamlColumn(path: Path, content: Content, source: Seq[Content]) = {
    NodeSeq.fromSeq(
      <div>Engine: <select class="publetRegisterEngine">{ engineOptions }</select></div>
      <h3>File browser</h3>
      <pre id="containerPath"></pre>
      <div id="filesTree"></div>).toString()
  }

  private def engineOptions: NodeSeq = {
    val publet = WebContext().publet
    val current = publet.resolveEngine(WebContext().requestPath)
    val opts = publet.engines.keySet.filter(_ != 'edit).map(s => {
      val tag = <option>{s.name}</option>
      if (current.exists(_.name==s)) tag % Attribute("selected", Text("selected"), Null) else tag
    })
    NodeSeq.fromSeq(opts.toSeq)
  }
}
