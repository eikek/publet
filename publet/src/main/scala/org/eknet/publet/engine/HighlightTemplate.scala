package org.eknet.publet.engine

import org.eknet.publet.impl.InstallCallback
import org.eknet.publet.{Publet, Content, Path}
import java.util.UUID
import org.eknet.publet.source.Partitions

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 31.03.12 22:12
 */
trait HighlightTemplate extends InstallCallback with HtmlTemplate {

  private val randomRoot = UUID.randomUUID().toString

  override def onInstall(publ: Publet) {
    super.onInstall(publ)
    publ.mount(Path("/"+randomRoot+"/highlight"), Partitions.highlightPartition)
  }

  override def headerContent(path: Path, content: Content) = {
    val base = path.relativeRoot + randomRoot + "/"
    super.headerContent(path, content) +
      """
      <link rel="stylesheet" href=""""+ base + "highlight/styles/idea.css" + """">
       <script src="""" + base + "highlight/highlight.pack.js"+ """"></script>
       <script>hljs.initHighlightingOnLoad();</script>
      """
  }

}
