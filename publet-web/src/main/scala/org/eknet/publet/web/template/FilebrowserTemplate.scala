package org.eknet.publet.web.template

import java.util.UUID
import org.eknet.publet.resource.Partition._
import org.eknet.publet.impl.InstallCallback
import org.eknet.publet.{Publet, Path}
import org.eknet.publet.resource.{ContentType, Content, NodeContent}
import xml.NodeSeq

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
    NodeSeq.fromSeq(<h3>File browser</h3>
    <pre id="containerPath"></pre>
    <div id="filesTree"></div>).toString()
  }
}
