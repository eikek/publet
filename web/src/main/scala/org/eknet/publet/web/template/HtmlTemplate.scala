package org.eknet.publet.web.template

import org.eknet.publet.vfs.{Path, Content, ContentType}

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
      case None => path.name.name.replaceAll("[_-]", " ")
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
  def htmlHead(path: Path, content: Content, source: Seq[Content]): String = ""

  /** Returns the html body part.
   * 
   * @param path
   * @param content
   * @return
   */
  def htmlBody(path: Path, content: Content, source: Seq[Content]): String = content.contentAsString
  
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
      htmlHead(path, content, source),
      htmlBody(path, content, source)) , ContentType.html)
  }
}
