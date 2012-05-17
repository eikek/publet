package org.eknet.publet.web.template

import org.eknet.publet.vfs.{Path, ContentResource, Content, ContentType}
import xml.PrettyPrinter


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 21:23
 */
trait HtmlTemplate {

  /** By default extracts the first {{{h*}}} value
   * 
   * @param content
   * @return
   */
  def title(path: Path, content: ContentResource, source: Seq[ContentResource]): String = {
    findHead(content) match {
      case None => content.name.name.replaceAll("[_-]", " ")
      case Some(n) => n
    }
  }

  val findHeadRegex = "(<h[1234]>([^<]*)</h[1234]>)".r

  def findHead(content: ContentResource): Option[String] = {
    findHeadRegex.findFirstMatchIn(content.contentAsString) match {
      case Some(mx) => Some(mx.group(2))
      case None => None
    }
  }

  /**Returns the html header part
   *
   * @param content
   * @return
   */
  def htmlHead(path: Path, content: ContentResource, source: Seq[ContentResource]): String = ""

  /** Returns the html body part.
   * 
   * @param content
   * @return
   */
  def htmlBody(path: Path, content: ContentResource, source: Seq[ContentResource]): String = content.contentAsString
  
  def charset = "utf-8"
  
  def apply(path: Path, content: ContentResource, source: Seq[ContentResource]): Option[Content] = {
    Predef.ensuring(content.contentType == ContentType.html, "Only html content possible")
    val pp = new PrettyPrinter(90, 2)
    val body = """<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
       "http://www.w3.org/TR/html4/loose.dtd">"""+ "\n"+
  pp.format(<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta charset={ charset }/>
      <title>%s</title>
      <meta http-equiv="CONTENT-LANGUAGE" content="EN"/>
      <meta name="revisit-after" content="14 days"/>
      <meta name="Generator" content="https://github.com/eikek/publet"/>
      %s
    </head>
    <body>
      %s
    </body>
  </html>)

    Some(Content(String.format(body,
      title(path, content, source),
      htmlHead(path, content, source),
      htmlBody(path, content, source)) , ContentType.html))
  }
}
