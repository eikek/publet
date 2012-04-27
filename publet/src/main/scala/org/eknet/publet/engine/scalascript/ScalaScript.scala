package org.eknet.publet.engine.scalascript

import com.twitter.json.Json
import org.eknet.publet.vfs.{ContentType, NodeContent, Content}
import xml.{NodeBuffer, NodeSeq}
import java.io.InputStream
import org.slf4j.LoggerFactory

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 10.04.12 21:31
 */

trait ScalaScript {
  protected val log = LoggerFactory.getLogger(getClass)

  def serve(): Content

}

object ScalaScript {

  def makeHtml(nseq: NodeSeq): NodeContent = NodeContent(nseq, ContentType.html)
  def makeHtml(nseq: NodeBuffer): NodeContent = NodeContent(nseq, ContentType.html)
  def makeHtml(str: String): Content = Content(str, ContentType.html)

  def makeJson(any: Any): Content = Content(Json.build(any).toString, ContentType.json)

  def makeJs(str: String): Content = Content(str, ContentType.javascript)

  def makePng(data: Array[Byte]): Content = Content(data, ContentType.png)
  def makePng(data: InputStream): Content = Content(data, ContentType.png)

  def makeJpg(data: Array[Byte]): Content = Content(data, ContentType.jpg)
  def makeJpg(data: InputStream): Content = Content(data, ContentType.jpg)
}

