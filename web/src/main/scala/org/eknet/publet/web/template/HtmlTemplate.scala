package org.eknet.publet.web.template

import org.eknet.publet.Path
import xml.{NodeSeq, Node}
import org.eknet.publet.resource.{Content, ContentType, NodeContent}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 21:23
 */
trait HtmlTemplate {

  /** By default extracts the first {{{h*}}} value
   * 
   * @param path
   * @param content
   * @return
   */
  def title(path: Path, content: Content, source: Seq[Content]): String = {
    findHead(content) match {
      case None => path.fileName.name.replaceAll("_", " ")
      case Some(n) => n
    }
  }

  val findHeadRegex = "(<h[1234]>([^<]*)</h[1234]>)".r

  def findHead(content: Content): Option[String] = {
    findHeadRegex.findFirstMatchIn(content.contentAsString) match {
      case Some(mx) => Some(mx.group(2))
      case None => None
    }
  }

  /**Returns the html header part
   *
   * @param path
   * @param content
   * @return
   */
  def headerHtml(path: Path, content: Content, source: Seq[Content]): String = ""

  /** Returns the html body part.
   * 
   * @param path
   * @param content
   * @return
   */
  def bodyHtml(path: Path, content: Content, source: Seq[Content]): String = content.contentAsString
  
  def charset = "utf-8"
  
  def apply(path: Path, content: Content, source: Seq[Content]): Content = {
    Predef.ensuring(content.contentType == ContentType.html, "Only html content possible")

    val body = """<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
       "http://www.w3.org/TR/html4/loose.dtd">"""+ "\n"+
  <html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta charset={ charset }/>
      <title>%s</title>
      %s
    </head>
    <body>
      %s
    </body>
  </html>.toString()

    Content(String.format(body,
      title(path, content, source),
      headerHtml(path, content, source),
      bodyHtml(path, content, source)) , ContentType.html)
  }
}
