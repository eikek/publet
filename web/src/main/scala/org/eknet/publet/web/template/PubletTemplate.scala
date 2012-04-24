package org.eknet.publet.web.template

import org.eknet.publet.impl.InstallCallback
import java.util.UUID
import org.eknet.publet.resource.Partition._
import org.eknet.publet.{Path, Publet}
import org.eknet.publet.resource.{ContentType, Content, NodeContent}
import xml.NodeSeq

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 13.04.12 23:25
 */
trait PubletTemplate extends HtmlTemplate with InstallCallback {

  private val randomRoot = UUID.randomUUID().toString

  val jqueryPartition = classpath(Path("../themes/browser"), classOf[PubletTemplate])

  override def onInstall(publ: Publet) {
    super.onInstall(publ)
    publ.mount(Path("/"+randomRoot+"/jquery"), jqueryPartition)
  }

  override def headerHtml(path: Path, content: Content, source: Seq[Content]) = {
    val base = path.relativeRoot + randomRoot + "/"
    super.headerHtml(path, content, source) +
    NodeSeq.fromSeq(
      <script src={ base + "jquery/jquery-1.7.2.min.js" }></script>
      <script src={ base + "jquery/publet.js" }></script>
    ).toString()
  }
}
