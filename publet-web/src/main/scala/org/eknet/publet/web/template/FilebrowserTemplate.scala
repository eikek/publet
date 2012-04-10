package org.eknet.publet.web.template

import java.util.UUID
import org.eknet.publet.resource.Partition._
import org.eknet.publet.impl.InstallCallback
import org.eknet.publet.{Publet, Path}
import org.eknet.publet.resource.NodeContent

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

  override def headerHtml(path: Path, content: NodeContent) = {
    val base = path.relativeRoot + randPath + "/"
    super.headerHtml(path, content) ++
    {
      <link type="text/css" rel="stylesheet" href={ base + "browser/browser.css" }></link>
      <script src={ base +"browser/jquery-1.7.2.min.js" } ></script>
      <script src={ base +"browser/browser.js" } ></script>
      <script>renderFileBrowser();</script>
    }
  }

  def yamlColumn(path: Path, content: NodeContent) = {
    <h3>File browser</h3>
    <pre id="containerPath"></pre>
    <div id="filesTree"></div>
  }
}
