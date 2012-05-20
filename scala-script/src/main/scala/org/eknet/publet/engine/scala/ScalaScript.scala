package org.eknet.publet.engine.scala

import org.eknet.publet.vfs.{ContentType, NodeContent, Content}
import xml.{NodeBuffer, NodeSeq}
import org.eknet.publet.com.twitter.json.Json
import java.io.InputStream
import grizzled.slf4j.Logging

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 10.04.12 21:31
 */

trait ScalaScript extends Logging {

  def serve(): Option[Content]

}

object ScalaScript {

  def makeSsp(nseq: NodeSeq): Option[NodeContent] = Some(NodeContent(nseq, ContentType.ssp))
  def makeSsp(nseq: NodeBuffer): Option[NodeContent] = Some(NodeContent(nseq, ContentType.ssp))
  def makeSsp(str: String): Option[Content] = Some(Content(str, ContentType.ssp))

  def makeJson(any: Any): Option[Content] = Some(Content(Json.build(any).toString, ContentType.json))

  def makeJs(str: String): Option[Content] = Some(Content(str, ContentType.javascript))

  def makePng(data: Array[Byte]): Option[Content] = Some(Content(data, ContentType.png))
  def makePng(data: InputStream): Option[Content] = Some(Content(data, ContentType.png))

  def makeJpg(data: Array[Byte]): Option[Content] = Some(Content(data, ContentType.jpg))
  def makeJpg(data: InputStream): Option[Content] = Some(Content(data, ContentType.jpg))
}