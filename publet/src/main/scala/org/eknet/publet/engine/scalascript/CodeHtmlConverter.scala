package org.eknet.publet.engine.scalascript

import org.eknet.publet.engine.convert.ConverterEngine
import org.eknet.publet.resource.ContentType._
import org.eknet.publet.resource.{NodeContent, Content}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 12.04.12 14:13
 */

class CodeHtmlConverter(langClass: Option[String]) extends ConverterEngine#Converter {
  def apply(v1: Content) = {
    val code = langClass match {
      case None => <code>{ v1.contentAsString }</code>
      case Some(c) => <code class={c}>{ v1.contentAsString }</code>
    }
      
    NodeContent(<pre>{code}</pre>, html)
  }
}

object CodeHtmlConverter {

  def apply(): CodeHtmlConverter = new CodeHtmlConverter(None)

  def scala: CodeHtmlConverter = new CodeHtmlConverter(Some("scala"))
  def json: CodeHtmlConverter = new CodeHtmlConverter(Some("json"))
  def css: CodeHtmlConverter = new CodeHtmlConverter(Some("css"))
}
