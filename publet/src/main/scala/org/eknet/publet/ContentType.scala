package org.eknet.publet

import tools.nsc.io.File

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 10:11
 */
case class ContentType(typeName: Symbol, extensions: Set[String], mime: (String, String))

object ContentType {

  val text = ContentType('text, Set("txt", "text"), ("text", "plain"))
  val html = ContentType('html, Set("html", "htm"), ("text", "html"))
  val pdf = ContentType('pdf, Set("pdf"), ("application", "pdf"))
  val markdown = ContentType('markdown, Set("md", "markdown"), ("text", "markdown"))
  val xml = ContentType('xml, Set("xml"), ("text", "xml"))
  val css = ContentType('css, Set("css"), ("text", "css"))
  val javascript = ContentType('javascript, Set("js"), ("text", "javascript"))
  val png = ContentType('png, Set("png"), ("image", "png"))
  val jpg = ContentType('jpg, Set("jpg", "jpeg"), ("image", "jpg"))
  val gif = ContentType('gif, Set("gif"), ("image", "gif"))

  val all = Seq(text, html, pdf, markdown, xml, css, javascript, png, jpg, gif)
  
  def apply(f: File): ContentType = apply(f.extension)
  
  def apply(ext: String): ContentType = {
    all.find(_.extensions.contains(ext.toLowerCase))
      .orElse(sys.error("unknown type: "+ ext)).get
  }

  def apply(name: Symbol): ContentType = {
    all.find(_.typeName == name)
      .orElse(sys.error("Unknown type: "+ name)).get
  }
  
  def apply(mime: (String, String)): ContentType = {
    all.find(_.mime == mime)
      .orElse(sys.error("Unknown mime type: "+ mime)).get
  }
  
  def forMimeBase(t: ContentType): Seq[ContentType] = all.filter(_.mime._1 == t.mime._1)
  
}
