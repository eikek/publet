package org.eknet.publet.web.template

import org.eknet.publet.vfs.{ContentResource, Content, ContentType}


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
  def title(content: ContentResource, source: Seq[ContentResource]): String = {
    val path = content.path
    findHead(content) match {
      case None => path.name.name.replaceAll("[_-]", " ")
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
  def htmlHead(content: ContentResource, source: Seq[ContentResource]): String = ""

  /** Returns the html body part.
   * 
   * @param content
   * @return
   */
  def htmlBody(content: ContentResource, source: Seq[ContentResource]): String = content.contentAsString
  
  def charset = "utf-8"
  
  def apply(content: ContentResource, source: Seq[ContentResource]): Option[Content] = {
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

    Some(Content(String.format(body,
      title(content, source),
      htmlHead(content, source),
      htmlBody(content, source)) , ContentType.html))
  }
}
