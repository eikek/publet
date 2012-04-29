package org.eknet.publet.web.template

import xml.NodeSeq
import org.eknet.publet.vfs.util.ClasspathContainer
import org.eknet.publet.Publet
import org.eknet.publet.vfs.{Content, Path}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 22:11
 */
object HighlightJs {

  val root = "/publet/highlightjs"

  def install(publet: Publet, e: StandardEngine) {
    val cp = new ClasspathContainer(classOf[HtmlTemplate], Some(Path("../themes/")))
    publet.mountManager.mount(Path(root), cp)

    e.htmlHeadContribs.append(htmlHeadSnippet)
  }

  def htmlHeadSnippet(path: Path, content: Content): String = {
    val base = (Path(path.relativeRoot) / Path(root).toRelative).asString+ "/"

    NodeSeq.fromSeq(<link rel="stylesheet" href={ base + "highlight/styles/"+ highlightStyle +".css" } ></link>
      <script src={ base + "highlight/highlight.pack.js" }></script>
      <script>hljs.initHighlightingOnLoad();</script>).toString()
  }

  val highlightStyle = HighlightJs.Styles.googlecode

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