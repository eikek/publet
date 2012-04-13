package org.eknet.publet.engine.scalascript

import com.twitter.json.Json
import org.eknet.publet.resource.{ContentType, NodeContent, Content}
import xml.{NodeBuffer, NodeSeq}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 10.04.12 21:31
 */

trait ScalaScript {

  def serve(): Content

}

object ScalaScript {
//  implicit def nodes2Content(n: NodeSeq): NodeContent = NodeContent(n, ContentType.html)
//  implicit def nodes2Content(n: NodeBuffer): NodeContent = NodeContent(n, ContentType.html)

  def makeHtml(nseq: NodeSeq): NodeContent = NodeContent(nseq, ContentType.html)
  def makeHtml(nseq: NodeBuffer): NodeContent = NodeContent(nseq, ContentType.html)
  def makeHtml(str: String): Content = Content(str, ContentType.html)

  def makeJson(any: Any): Content = Content(Json.build(any).toString, ContentType.json)
}

