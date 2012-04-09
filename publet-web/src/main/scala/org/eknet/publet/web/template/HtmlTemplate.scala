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
  def title(path: Path, content: NodeContent): String = {
    findHead(content, 1, 4) match {
      case None => path.fileName.name
      case Some(n) => n.text
    }
  }
  
  def findHead(content: NodeContent, c: Int, m: Int): Option[Node] = {
    try {
      val nodeseq = content.node \\ ("h" + c)
      if (nodeseq == NodeSeq.Empty) {
        if (c < m) findHead(content, c + 1, m) else None
      } else Some(nodeseq.head)
    }
    catch {
      case e => None
    }
  }

  /**Returns the html header part
   *
   * @param path
   * @param content
   * @return
   */
  def headerHtml(path: Path, content: NodeContent): NodeSeq = NodeSeq.Empty

  /** Returns the html body part.
   * 
   * @param path
   * @param content
   * @return
   */
  def bodyHtml(path: Path, content: NodeContent): NodeSeq = content.node
  
  def charset = "utf-8"
  
  def apply(path: Path, content: NodeContent): Content = {
    val body = """<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
       "http://www.w3.org/TR/html4/loose.dtd">"""+ "\n"+
  <html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta charset={ charset }/>
      <title>{ title(path, content) }</title>
      { headerHtml(path, content) }
    </head>
    <body>
      { bodyHtml(path, content) }
    </body>
  </html>.toString()
    Content(body, ContentType.html)
  }
}
