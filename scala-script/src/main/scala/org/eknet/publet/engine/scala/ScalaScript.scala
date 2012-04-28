package org.eknet.publet.engine.scala

import org.slf4j.LoggerFactory
import org.eknet.publet.vfs.{ContentType, NodeContent, Content}
import xml.{NodeBuffer, NodeSeq}
import org.eknet.publet.com.twitter.json.Json
import java.io.InputStream

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 10.04.12 21:31
 */

trait ScalaScript {
  protected val log = LoggerFactory.getLogger(getClass)

  def serve(): Option[Content]

}

object ScalaScript {

  def makeHtml(nseq: NodeSeq): Option[NodeContent] = Some(NodeContent(nseq, ContentType.html))
  def makeHtml(nseq: NodeBuffer): Option[NodeContent] = Some(NodeContent(nseq, ContentType.html))
  def makeHtml(str: String): Option[Content] = Some(Content(str, ContentType.html))

  def makeJson(any: Any): Option[Content] = Some(Content(Json.build(any).toString, ContentType.json))

  def makeJs(str: String): Option[Content] = Some(Content(str, ContentType.javascript))

  def makePng(data: Array[Byte]): Option[Content] = Some(Content(data, ContentType.png))
  def makePng(data: InputStream): Option[Content] = Some(Content(data, ContentType.png))

  def makeJpg(data: Array[Byte]): Option[Content] = Some(Content(data, ContentType.jpg))
  def makeJpg(data: InputStream): Option[Content] = Some(Content(data, ContentType.jpg))
}