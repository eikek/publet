package org.eknet.publet.web.template

import org.eknet.publet.impl.InstallCallback
import java.util.UUID
import org.eknet.publet.{Publet, Path}
import org.eknet.publet.resource.NodeContent
import org.eknet.publet.resource.Partition._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 22:11
 */
trait HighlightTemplate extends InstallCallback with HtmlTemplate {

  private val randomRoot = UUID.randomUUID().toString

  val highlightPartition = classpath(Path("../themes/highlight"), classOf[HighlightTemplate])

  override def onInstall(publ: Publet) {
    super.onInstall(publ)
    publ.mount(Path("/"+randomRoot+"/highlight"), highlightPartition)
  }

  override def headerHtml(path: Path, content: NodeContent) = {
    val base = path.relativeRoot + randomRoot + "/"
    super.headerHtml(path, content) ++
    {
      <link rel="stylesheet" href={ base + "highlight/styles/"+ highlightStyle +".css" } ></link>
      <script src={ base + "highlight/highlight.pack.js" }></script>
      <script>hljs.initHighlightingOnLoad();</script>
    }
  }
  
  def highlightStyle = HighlightTemplate.Styles.googlecode
}

object HighlightTemplate {

  object Styles extends Enumeration {
    val arta = Value("arta")
    val ascetic = Value("ascetic")
    val brown_paper = Value("brown_paper")
    val dark = Value("dark")
    val default = Value("default")
    val far = Value("far")
    val github = Value("github")
    val googlecode = Value("googlecode")
    val idea = Value("idea")
    val irblack = Value("ir_black")
    val magula = Value("magula")
    val monokai = Value("monokai")
    val schoolbook = Value("school_book")
    val solarizedlight = Value("solarized_light")
    val solarizeddark = Value("solarized_dark")
    val sunburst = Value("sunburst")
    val vs = Value("vs")
    val zenburn = Value("zenburn")
  }
}