package org.eknet.publet

import tools.nsc.io.File

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 10:11
 */
case class ContentType(typeName: Symbol, extensions: Set[String]) {

}

object ContentType {

  val text = ContentType('text, Set("txt", "text"))
  val html = ContentType('html, Set("html", "htm"))
  val pdf = ContentType('pdf, Set("pdf"))
  val markdown = ContentType('markdown, Set("md", "markdown"))
  val xml = ContentType('xml, Set("xml"))
  val css = ContentType('css, Set("css"))
  val javascript = ContentType('javascript, Set("js"))
  val png = ContentType('png, Set("png"))
  val jpg = ContentType('jpg, Set("jpg", "jpeg"))
  val gif = ContentType('gif, Set("gif"))

  val all = Seq(text, html, pdf, markdown, xml, css, javascript, png, jpg, gif)
  
  def apply(f: File): ContentType = apply(f.extension)
  
  def apply(ext: String): ContentType = {
    all.find(_.extensions.contains(ext.toLowerCase))
      .orElse(sys.error("unknown type: "+ ext)).get
  }
}
