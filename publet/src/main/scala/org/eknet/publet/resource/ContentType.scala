package org.eknet.publet.resource

import java.io.File
import org.eknet.publet.Path.apply
import org.eknet.publet.Path

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 10:11
 */
case class ContentType(typeName: Symbol, extensions: Set[String], mime: (String, String)) {

  val mimeString = mime._1 +"/"+ mime._2
}

object ContentType {

  val unknown = ContentType('unknown, Set(), ("application", "octet-stream"))
  val scal = ContentType('scala, Set("scala"), ("text", "scala"))
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
  val json = ContentType('json, Set("json"), ("text", "plain"))
  val icon = ContentType('icon, Set("ico"), ("image", "x-icon"))

  val all = Seq(text, html, pdf, markdown, xml, css, javascript, png, jpg, gif, icon, scal)

  def apply(f: File): ContentType = apply(extension(f))

  private def extension(f: File): String = Path(f).extension.get

  def apply(ext: String): ContentType = {
    all.find(_.extensions.contains(ext.toLowerCase))
      .orElse(error("unknown type: " + ext)).get
  }

  def apply(name: Symbol): ContentType = {
    all.find(_.typeName == name)
      .orElse(error("Unknown type: " + name)).get
  }

  def apply(mime: (String, String)): ContentType = {
    all.find(_.mime == mime)
      .orElse(error("Unknown mime type: " + mime)).get
  }

  def forMimeBase(t: ContentType): Seq[ContentType] = all.filter(_.mime._1 == t.mime._1)

}
