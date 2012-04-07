package org.eknet.publet.web.template

import java.util.UUID
import org.eknet.publet.resource.Partition._
import org.eknet.publet.impl.InstallCallback
import org.eknet.publet.{Publet, Path}
import org.eknet.publet.resource.{ContentType, NodeContent}
import xml.Attribute._
import xml.Text._
import xml.{Attribute, Text, Null, NodeSeq}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 07.04.12 21:28
 */

trait EditTemplate extends HtmlTemplate with InstallCallback {

  private val randPath = UUID.randomUUID().toString

  val editPartition = classpath(Path("../themes/browser"), classOf[EditTemplate])


  override def onInstall(publ: Publet) {
    super.onInstall(publ)
    publ.mount(Path("/"+randPath+"/browser"), editPartition)
  }

  override def headerHtml(path: Path, content: NodeContent) = {
    val base = path.relativeRoot + randPath + "/"
    super.headerHtml(path, content) ++
    {
      <script src={ base +"browser/jquery-1.7.2.min.js" } ></script>
      <script src={ base +"browser/browser.js" } ></script>
      <script>renderFileBrowser();</script>
    }
  }

}
