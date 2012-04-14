package org.eknet.publet.web.template

import org.eknet.publet.impl.InstallCallback
import org.eknet.publet.{Publet, Path}
import org.eknet.publet.resource.{Content, NodeContent}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 13.04.12 20:03
 */

trait CustomCssTemplate extends HtmlTemplate {

  override def headerHtml(path: Path, content: NodeContent, source: Seq[Content]) = {
    val css = path.sibling(customCssFile)
    if (publet.lookup(css).isDefined) {
      super.headerHtml(path, content, source) ++
      {
        <link rel="stylesheet" href={ customCssFile }></link>
      }
    } else {
      super.headerHtml(path, content, source)
    }
  }

  def publet: Publet

  def customCssFile = "style.css"
}
